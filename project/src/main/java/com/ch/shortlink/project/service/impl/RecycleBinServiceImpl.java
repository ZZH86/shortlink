package com.ch.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.shortlink.project.common.constant.RedisKeyConstant;
import com.ch.shortlink.project.common.convention.exception.ServiceException;
import com.ch.shortlink.project.dao.entity.ShortLinkDO;
import com.ch.shortlink.project.dao.mapper.ShortLinkMapper;
import com.ch.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ch.shortlink.project.dto.req.ShortLinkSaveRecycleBinReqDTO;
import com.ch.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.ch.shortlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @Author hui cao
 * @Description: 回收站接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 移至回收站
     *
     * @param requestParam 移至回收站请求参数
     */
    @Override
    public void saveRecycleBin(ShortLinkSaveRecycleBinReqDTO requestParam) {
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0);
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .enableStatus(1)
                .build();

        int update = baseMapper.update(shortLinkDO, updateWrapper);
        if (update < 1) {
            throw new ServiceException("移至回收站失败");
        }
        // 从缓存中删除跳转原始链接
        stringRedisTemplate.delete(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
    }

    /**
     * 分页查询回收站
     *
     * @param requestParam 分页查询请求参数
     * @return 分页查询返回对象
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 1)
                .orderByDesc(ShortLinkDO::getCreateTime);
        IPage<ShortLinkDO> results = baseMapper.selectPage(requestParam, queryWrapper);
        return results.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            result.setFullShortUrl(result.getDomain() + "/" + result.getShortUri());
            return result;
        });
    }
}
