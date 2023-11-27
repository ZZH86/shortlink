package com.ch.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.shortlink.project.common.constant.RedisKeyConstant;
import com.ch.shortlink.project.common.convention.exception.ServiceException;
import com.ch.shortlink.project.dao.entity.ShortLinkDO;
import com.ch.shortlink.project.dao.entity.ShortLinkGotoDO;
import com.ch.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.ch.shortlink.project.dao.mapper.ShortLinkMapper;
import com.ch.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ch.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ch.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.ch.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.ch.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.ch.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.ch.shortlink.project.service.ShortLinkService;
import com.ch.shortlink.project.toolkit.HashUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

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

        // 通过 redis 来存储短连接的原始链接，防止缓存击穿
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl));

        // 如果存在，直接进行跳转
        if (StrUtil.isNotBlank(originalLink)) {
            try {
                ((HttpServletResponse) response).sendRedirect(originalLink);
                return;
            } catch (IOException e) {
                throw new ServiceException("跳转失败OvO");
            }
        }

        // 如果不存在，则需要查询数据库并进行回写,引入分布式锁防止大量不存在数据查询数据库
        RLock lock = redissonClient.getLock(String.format(RedisKeyConstant.LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();

        try {
            // 双重判定锁，如果有很多个请求已经到达 lock ，就没必要全部查询数据库，一个请求回写后走 redis
            originalLink = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originalLink)) {
                try {
                    ((HttpServletResponse) response).sendRedirect(originalLink);
                    return;
                } catch (IOException e) {
                    throw new ServiceException("跳转失败OvO");
                }
            }

            // 通过 goto 表查到 gid
            LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(queryWrapper);
            if (shortLinkGotoDO == null) {
                // TODO 此处要进行风控
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

            // 原始链接存在，进行 302 重定向跳转
            if (shortLinkDO != null) {
                String originShortLink = shortLinkDO.getOriginUrl();
                try {
                    // 将查询到的原始链接回写进数据库
                    stringRedisTemplate.opsForValue().set(
                            String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, shortLinkDO.getFullShortUrl()), originShortLink);
                    ((HttpServletResponse) response).sendRedirect(originShortLink);
                } catch (IOException e) {
                    throw new ServiceException("跳转失败OvO");
                }
            }
        } finally {
            lock.unlock();
        }
    }


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


}
