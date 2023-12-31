package com.ch.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.shortlink.project.common.constant.RedisKeyConstant;
import com.ch.shortlink.project.common.convention.exception.ServiceException;
import com.ch.shortlink.project.dao.entity.ShortLinkDO;
import com.ch.shortlink.project.dao.mapper.ShortLinkMapper;
import com.ch.shortlink.project.dto.req.*;
import com.ch.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.ch.shortlink.project.service.RecycleBinService;
import com.ch.shortlink.project.service.ShortLinkService;
import com.ch.shortlink.project.toolkit.LinkUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author hui cao
 * @Description: 回收站接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;

    private final ShortLinkService shortLinkService;

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
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .in(ShortLinkDO::getGid, requestParam.getGidList())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 1)
                .orderByDesc(ShortLinkDO::getUpdateTime);
        IPage<ShortLinkDO> results = baseMapper.selectPage(requestParam, queryWrapper);
        return results.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
//            result.setDomain("http://" + result.getDomain());
            result.setFullShortUrl(result.getDomain() + "/" + result.getShortUri());
            return result;
        });
    }

    /**
     * 恢复回收站短链接
     *
     * @param requestParam 恢复回收站短链接请求参数
     */
    @Override
    public void recoverRecycleBinShortLink(ShortLinkRecoverRecycleBinReqDTO requestParam) {
        // 如果短链接的分组已经被删除了，那么就把它移到默认分组进行恢复
        String gid = requestParam.getGid();
        String defaultGid = requestParam.getDefaultGid();
        
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, gid)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getDelFlag, 0);
        ShortLinkDO shortLinkDO = ShortLinkDO.builder().enableStatus(0).build();

        int update = baseMapper.update(shortLinkDO, updateWrapper);
        if (update < 1) {
            throw new ServiceException("恢复回收站短链接失败");
        }

        // 此时虽然恢复了，但是分组还没有变，如果分组被删除了，就进行更改分组操作
        if(StrUtil.isNotBlank(defaultGid)){
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, gid)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .eq(ShortLinkDO::getDelFlag, 0);
            ShortLinkDO updateShortLink = baseMapper.selectOne(queryWrapper);
            updateShortLink.setGid(defaultGid);
            ShortLinkUpdateReqDTO linkUpdateReqDTO = ShortLinkUpdateReqDTO.builder()
                    .originUrl(updateShortLink.getOriginUrl())
                    .fullShortUrl(updateShortLink.getFullShortUrl())
                    .originGid(gid)
                    .gid(defaultGid)
                    .validDateType(updateShortLink.getValidDateType())
                    .validDate(updateShortLink.getValidDate())
                    .describe(updateShortLink.getDescribe())
                    .build();
            // 更新分组
            shortLinkService.updateShortLink(linkUpdateReqDTO);
            // 更新它的 gid 方便后续操作
            gid = defaultGid;
        }

        // 拿到更新后的短链接做缓存预热
        LambdaQueryWrapper<ShortLinkDO> linkDOLambdaQueryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, gid)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0);
        ShortLinkDO newShortLinkDO = baseMapper.selectOne(linkDOLambdaQueryWrapper);
        // 将恢复的短链接重新加入到缓存中(缓存预热)
        stringRedisTemplate.opsForValue().set(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()), newShortLinkDO.getOriginUrl(), LinkUtil.getLinkCacheValidTime(newShortLinkDO.getValidDate()), TimeUnit.MILLISECONDS);
        //把原来可能还存在的缓存的空值删除
        stringRedisTemplate.delete(String.format(RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
    }

    /**
     * 删除回收站短链接
     *
     * @param requestParam 删除回收站短链接请求参数
     */
    @Override
    public void removeRecycleBinShortLink(RecycleBinRemoveReqDTO requestParam) {
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getDelTime, 0L)
                .eq(ShortLinkDO::getDelFlag, 0);
        baseMapper.delete(updateWrapper);
        ShortLinkDO delShortLinkDO = ShortLinkDO.builder()
                .delTime(System.currentTimeMillis())
                .build();
        delShortLinkDO.setDelFlag(1);
        int delete = baseMapper.update(delShortLinkDO, updateWrapper);
        if (delete < 1) {
            throw new ServiceException("删除回收站短链接失败");
        }
    }

    /**
     * 整个分组移至回收站
     *
     * @param requestParam 整个分组移至回收站请求参数
     */
    @Override
    public void saveBatchRecycleBin(ShortLinkSaveBatchRecycleBinReqDTO requestParam) {
        String gid = requestParam.getGid();
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, gid)
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0);
        List<ShortLinkDO> shortLinkDOList = baseMapper.selectList(queryWrapper);

        for (ShortLinkDO shortLinkDO : shortLinkDOList) {
            ShortLinkSaveRecycleBinReqDTO param = ShortLinkSaveRecycleBinReqDTO.builder()
                    .gid(gid)
                    .fullShortUrl(shortLinkDO.getFullShortUrl())
                    .build();
            saveRecycleBin(param);
        }
    }
}
