package com.ch.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.shortlink.project.common.convention.exception.ServiceException;
import com.ch.shortlink.project.dao.entity.ShortLinkDO;
import com.ch.shortlink.project.dao.mapper.ShortLinkMapper;
import com.ch.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ch.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ch.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.ch.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.ch.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.ch.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.ch.shortlink.project.service.ShortLinkService;
import com.ch.shortlink.project.toolkit.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 创建短链接
     *
     * @param requestParam 创建短链接请求对象
     * @return 创建短链接响应对象
     */
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        String shortLinkCode = getShortLinkCode(requestParam);
        String fullShortUrl = StrBuilder.create(requestParam.getDomain())
                .append("/")
                .append(shortLinkCode)
                .toString();
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
        try {
            baseMapper.insert(shortLinkDO);
        } catch (Exception e) {
            log.warn("短链接：{} 重复入库", fullShortUrl);
            throw new ServiceException("重复创建");
        }
        shortLinkBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .gid(shortLinkDO.getGid())
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .originUrl(shortLinkDO.getOriginUrl())
                .build();
    }

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
        if(shortLinkDO == null){
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
        return resultPage.convert(each -> BeanUtil.toBean(each, ShortLinkPageRespDTO.class));
    }

    /**
     * 查询短链接分组内短链接数量
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
