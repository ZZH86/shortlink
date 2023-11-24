package com.ch.shortlink.project.service.impl;

import cn.hutool.core.text.StrBuilder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.shortlink.project.common.convention.exception.ServiceException;
import com.ch.shortlink.project.dao.entity.ShortLinkDO;
import com.ch.shortlink.project.dao.mapper.ShortLinkMapper;
import com.ch.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ch.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.ch.shortlink.project.service.ShortLinkService;
import com.ch.shortlink.project.toolkit.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.stereotype.Service;

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
