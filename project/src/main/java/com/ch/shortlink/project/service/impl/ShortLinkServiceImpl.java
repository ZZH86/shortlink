package com.ch.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.shortlink.project.common.constant.RedisKeyConstant;
import com.ch.shortlink.project.common.constant.ShortLinkConstant;
import com.ch.shortlink.project.common.convention.exception.ServiceException;
import com.ch.shortlink.project.dao.entity.*;
import com.ch.shortlink.project.dao.mapper.*;
import com.ch.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ch.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ch.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.ch.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.ch.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.ch.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.ch.shortlink.project.service.ShortLinkService;
import com.ch.shortlink.project.toolkit.HashUtil;
import com.ch.shortlink.project.toolkit.LinkUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author hui cao
 * @Description: 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    // 注入短链接布隆过滤器
    private final RBloomFilter<String> shortLinkBloomFilter;

    private final ShortLinkGotoMapper shortLinkGotoMapper;

    private final LinkAccessStatsMapper linkAccessStatsMapper;

    private final LinkLocalStatsMapper linkLocalStatsMapper;

    private final LinkOsStatsMapper linkOsStatsMapper;

    private final LinkBrowserStatsMapper linkBrowserStatsMapper;

    private final LinkAccessLogsMapper linkAccessLogsMapper;

    private final LinkDeviceStatsMapper linkDeviceStatsMapper;

    private final LinkNetworkStatsMapper linkNetworkStatsMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    @Value("${short-link.stats.local.amap-key}")
    private String statsLocalAmapKey;

    /**
     * 创建短链接
     *
     * @param requestParam 创建短链接请求对象
     * @return 创建短链接响应对象
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {

        // 获取六位数短链接
        String shortLinkCode = getShortLinkCode(requestParam);
        String fullShortUrl = StrBuilder.create(requestParam.getDomain())
                .append("/")
                .append(shortLinkCode)
                .toString();

        // 新创建的短链接构建
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(requestParam.getDomain())
                .shortUri(shortLinkCode)
                .fullShortUrl(fullShortUrl)
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .enableStatus(0)
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .favicon(LinkUtil.getWebsiteIcon(requestParam.getOriginUrl()))
                .build();

        // 新建同时插入到 shortLinkGoto 表
        ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO.builder()
                .gid(requestParam.getGid())
                .fullShortUrl(fullShortUrl)
                .build();
        try {
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(shortLinkGotoDO);
        } catch (Exception e) {
            log.warn("短链接：{} 重复入库", fullShortUrl);
            throw new ServiceException("重复创建");
        }

        // 缓存预热
        stringRedisTemplate.opsForValue().set(
                String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, shortLinkDO.getFullShortUrl()),
                requestParam.getOriginUrl(),
                LinkUtil.getLinkCacheValidTime(requestParam.getValidDate()),
                TimeUnit.MILLISECONDS
        );

        // 添加到布隆过滤器
        shortLinkBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .gid(shortLinkDO.getGid())
                .fullShortUrl("http://" + shortLinkDO.getFullShortUrl())
                .originUrl(shortLinkDO.getOriginUrl())
                .build();
    }

    /**
     * 修改短链接
     *
     * @param requestParam 修改短链接请求参数
     */
    @Transactional(rollbackFor = Exception.class)    // TODO 目前其实不用加
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        // TODO 这里目前不做修改 gid 的操作，后续优化

        // 根据 gid 和 url 查询记录是否存在
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
        if (shortLinkDO == null) {
            throw new ServiceException("短链接记录不存在");
        }
        ShortLinkDO updateShortLinkDO = ShortLinkDO.builder()
                .domain(shortLinkDO.getDomain())
                .shortUri(shortLinkDO.getShortUri())
                .clickNum(shortLinkDO.getClickNum())
                .favicon(shortLinkDO.getFavicon())
                .createdType(shortLinkDO.getCreatedType())
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .describe(requestParam.getDescribe())
                .validDate(requestParam.getValidDate())
                .validDateType(requestParam.getValidDateType())
                .build();

        baseMapper.update(updateShortLinkDO, queryWrapper);
    }

    /**
     * 分页查询短链接
     *
     * @param requestParam 分页查询短链接请求参数
     * @return 短链接分页响应对象
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0)
                .orderByDesc(ShortLinkDO::getCreateTime);
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            result.setFullShortUrl(result.getDomain() + "/" + result.getShortUri());
            return result;
        });
    }

    /**
     * 查询短链接分组内短链接数量
     *
     * @param requestParam 分组标识列表
     * @return 分组数量响应对象列表
     */
    @Override
    public List<ShortLinkGroupCountQueryRespDTO> linkGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid, count(*) as ShortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .groupBy("gid");
        List<Map<String, Object>> list = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(list, ShortLinkGroupCountQueryRespDTO.class);
    }

    /**
     * 短链接跳转
     *
     * @param shortUri 短链接后缀
     * @param request  http 请求
     * @param response http 响应
     */
    @Override
    public void restoreUri(String shortUri, ServletRequest request, ServletResponse response) {
        String serverName = request.getServerName();
        String fullShortUrl = StrBuilder.create(serverName).append("/").append(shortUri).toString();

        // 通过 redis 来存储短连接的原始链接，防止缓存击穿（缓存穿透）
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl));

        // 如果存在，直接进行跳转
        if (StrUtil.isNotBlank(originalLink)) {
            // 访问统计
            shortLinkStats(fullShortUrl, null, request, response);
            // 重定向
            sendRedirect(response, originalLink);
            return;
        }

        // 如果布隆过滤器不存在，那么数据库也一定不存在（缓存击穿）
        if (!shortLinkBloomFilter.contains(fullShortUrl)) {
            sendRedirect(response, "/page/notfound");
            return;
        }

        // 判断是否已经将该链接缓存为空值（缓存击穿）
        if (StrUtil.isNotBlank(stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl)))) {
            sendRedirect(response, "/page/notfound");
            return;
        }

        // 如果不存在，则需要查询数据库并进行回写,引入分布式锁防止大量不存在数据查询数据库
        RLock lock = redissonClient.getLock(String.format(RedisKeyConstant.LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();

        try {
            // 双重判定锁，如果有很多个请求已经到达 lock ，就没必要全部查询数据库，一个请求回写后走 redis
            originalLink = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originalLink)) {
                // 访问统计
                shortLinkStats(fullShortUrl, null, request, response);
                // 重定向
                sendRedirect(response, originalLink);
                return;
            }

            // 通过 goto 表查到 gid
            LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(queryWrapper);

            if (shortLinkGotoDO == null) {
                // 如果数据库也没有，就将该短链接缓存为空值,30 分钟过期时间+一个随机数（缓存击穿）
                int timeout = RandomUtil.randomInt(500) + 1800;
                stringRedisTemplate.opsForValue().set(
                        String.format(RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl),
                        "hi~", timeout, TimeUnit.SECONDS);
                sendRedirect(response, "/page/notfound");
                return;
            }
            String gid = shortLinkGotoDO.getGid();

            // 通过 gid 查询 t_link 表找到对应的原始链接
            LambdaQueryWrapper<ShortLinkDO> linkDOLambdaQueryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, gid)
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .eq(ShortLinkDO::getDelFlag, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(linkDOLambdaQueryWrapper);


            if (shortLinkDO == null || (shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().before(new Date()))) {
                // 判断是否过期,过期的话就当空值进行回写 redis，防止穿透
                // 或者移入了回收站也缓存空对象
                int timeout = RandomUtil.randomInt(500) + 1800;
                stringRedisTemplate.opsForValue().set(
                        String.format(RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl),
                        "hi~", timeout, TimeUnit.SECONDS);
                sendRedirect(response, "/page/notfound");
                return;
            }

            // 原始链接存在，写入缓存并进行 302 重定向跳转
            String originShortLink = shortLinkDO.getOriginUrl();

            // 将查询到的原始链接回写进数据库 （缓存穿透）
            stringRedisTemplate.opsForValue().set(
                    String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, shortLinkDO.getFullShortUrl()),
                    shortLinkDO.getOriginUrl(),
                    LinkUtil.getLinkCacheValidTime(shortLinkDO.getValidDate()),
                    TimeUnit.MILLISECONDS
            );

            // 访问统计
            shortLinkStats(fullShortUrl, shortLinkDO.getGid(), request, response);

            // 重定向
            sendRedirect(response, originShortLink);
        } finally {
            lock.unlock();
        }
    }


    /**
     * 获得六位数短链接
     *
     * @param requestParam 请求参数
     * @return 六位短链接
     */
    private String getShortLinkCode(ShortLinkCreateReqDTO requestParam) {
        String originUrl = requestParam.getOriginUrl();
        String shortUri;
        int customGenerateCount = 0;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接生成频繁，请稍后再试");
            }
            originUrl += (System.currentTimeMillis());
            shortUri = HashUtil.hashToBase62(originUrl);
            if (!(shortLinkBloomFilter.contains(requestParam.getDomain() + "/" + shortUri))) {
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }

    /**
     * 短链接重定向
     *
     * @param response 响应对象
     * @param url      跳转链接
     */
    private void sendRedirect(ServletResponse response, String url) {
        try {
            ((HttpServletResponse) response).sendRedirect(url);
        } catch (IOException e) {
            throw new ServiceException("跳转失败OvO");
        }
    }

    /**
     * 短链接访问统计
     *
     * @param fullShortUrl 完整短链接
     * @param gid          分组标识
     * @param request      请求
     * @param response     响应
     */
    private void shortLinkStats(String fullShortUrl, String gid, ServletRequest request, ServletResponse response) {

        // 获取请求中的 cookie
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();

        // stream 流用原子类
        AtomicBoolean uvFirstFlag = new AtomicBoolean();

        AtomicReference<String> uv = new AtomicReference<>();
        // 定义设置添加 uvCookie 的函数
        Runnable addResponseCookieTask = () -> {
            uv.set(UUID.fastUUID().toString());
            Cookie uvCookie = new Cookie("uv", uv.get());
            uvCookie.setMaxAge(60 * 60 * 24 * 30);
            uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            ((HttpServletResponse) response).addCookie(uvCookie);
            uvFirstFlag.set(Boolean.TRUE);
            stringRedisTemplate.opsForSet().add(
                    String.format(RedisKeyConstant.UV_STATS_SHORT_LINK_KEY, fullShortUrl), uv.get());
        };
        try {

            // 不为空：获取 uv 的值，添加到 redis set缓存,添加失败代表已经添加过，flag 被设置为 false，设置标识
            if (ArrayUtil.isNotEmpty(cookies)) {
                Arrays.stream(cookies)
                        .filter(cookie -> Objects.equals(cookie.getName(), "uv"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .ifPresentOrElse(each -> {
                                    uv.set(each);
                                    Long uvAdd = stringRedisTemplate.opsForSet().add(
                                            String.format(RedisKeyConstant.UV_STATS_SHORT_LINK_KEY, fullShortUrl), each);
                                    uvFirstFlag.set(uvAdd != null && uvAdd > 0L);
                                }, addResponseCookieTask
                        );
            }
            // 为空：直接设置 uv，并缓存
            else {
                addResponseCookieTask.run();
            }

            // ***** uip 统计操作 *****
            String actualIp = LinkUtil.getActualIp((HttpServletRequest) request);
            Long uipAdd = stringRedisTemplate.opsForSet().add(
                    String.format(RedisKeyConstant.UIP_STATS_SHORT_LINK_KEY, fullShortUrl), actualIp);
            boolean uipFirstFlag = (uipAdd != null && uipAdd > 0L);

            // gid 为空是否去 goto 表获取
            if (gid == null) {
                LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(queryWrapper);
                gid = shortLinkGotoDO.getGid();
            }
            int hour = DateUtil.hour(new Date(), true);
            Week week = DateUtil.dayOfWeekEnum(new Date());
            int weekIso8601Value = week.getIso8601Value();
            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(uvFirstFlag.get() ? 1 : 0)
                    .uip(uipFirstFlag ? 1 : 0)
                    .hour(hour)
                    .weekday(weekIso8601Value)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);

            // ***** 地区统计 *****
            // 利用 get 请求通过 IP 和 高德key 去获取到高德 api 的返回参数
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("ip", actualIp);
            hashMap.put("key", statsLocalAmapKey);
            String localResultStr = HttpUtil.get(ShortLinkConstant.AMAP_REMOTE_URL, hashMap);
            JSONObject localResultObj = JSON.parseObject(localResultStr);

            // 根据返回参数来进行
            String infocode = localResultObj.getString("infocode");
            LinkLocalStatsDO linkLocalStatsDO;
            String actualProvince = "未知";
            String actualCity = "未知";
            if (StrUtil.isNotBlank(infocode) && StrUtil.equals(infocode, "10000")) {
                String province = localResultObj.getString("province");
                boolean isBlankFlag = Objects.equals("[]", province);
                linkLocalStatsDO = LinkLocalStatsDO.builder()
                        .province(actualProvince = (isBlankFlag ? "未知" : province))
                        .adcode(isBlankFlag ? "未知" : localResultObj.getString("adcode"))
                        .city(actualCity = (isBlankFlag ? "未知" : localResultObj.getString("city")))
                        .country("中国")
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .date(new Date())
                        .build();
                linkLocalStatsMapper.shortLinkLocaleState(linkLocalStatsDO);
            }

            // ***** os 统计 *****
            String os = LinkUtil.getOs((HttpServletRequest) request);
            LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                    .os(os)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .cnt(1)
                    .date(new Date())
                    .build();
            linkOsStatsMapper.shortLinkOsState(linkOsStatsDO);

            // ***** 访问浏览器统计 *****
            String browser = LinkUtil.getBrowser((HttpServletRequest) request);
            LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                    .browser(browser)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .cnt(1)
                    .date(new Date())
                    .build();
            linkBrowserStatsMapper.shortLinkBrowserState(linkBrowserStatsDO);

            // ***** 访问设备统计 *****
            String device = LinkUtil.getDevice(((HttpServletRequest) request));
            LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                    .device(device)
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .build();
            linkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);

            // ***** 访问网络统计 *****
            String network = LinkUtil.getNetwork(((HttpServletRequest) request));
            LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                    .network(network)
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .build();
            linkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);

            // ***** 访问日志统计 *****
            LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .ip(actualIp)
                    .browser(browser)
                    .user(uv.get())
                    .os(os)
                    .local(StrBuilder.create("中国-").append(actualProvince).append("-").append(actualCity).toString())
                    .network(network)
                    .device(device)
                    .build();
            linkAccessLogsMapper.insert(linkAccessLogsDO);
        } catch (Exception ex) {
            log.error("短连接流量访问统计异常", ex);
        }


    }


}
