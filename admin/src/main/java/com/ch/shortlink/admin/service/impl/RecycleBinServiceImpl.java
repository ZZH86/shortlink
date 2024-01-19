package com.ch.shortlink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ch.shortlink.admin.common.biz.user.UserContext;
import com.ch.shortlink.admin.common.convention.exception.ServiceException;
import com.ch.shortlink.admin.common.convention.result.Result;
import com.ch.shortlink.admin.dao.entity.GroupDO;
import com.ch.shortlink.admin.dao.mapper.GroupMapper;
import com.ch.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.ch.shortlink.admin.remote.dto.req.ShortLinkRecoverRecycleBinReqDTO;
import com.ch.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.ch.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.ch.shortlink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author hui cao
 * @Description: URL 回收站接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    private final GroupMapper groupMapper;

    /**
     * 分页查询回收站
     *
     * @param requestParam 分页查询回收站请求参数
     * @return 分页查询回收站返回对象
     */
    @Override
    public Result<Page<ShortLinkPageRespDTO>> pageRecycleShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername());
        List<GroupDO> groupDOList = groupMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(groupDOList)) {
            throw new ServiceException("用户无分组信息");
        }
        requestParam.setGidList(groupDOList.stream().map(GroupDO::getGid).toList());
        return shortLinkActualRemoteService.pageRecycleShortLink(requestParam.getGidList(), requestParam.getCurrent(), requestParam.getSize());
    }

    /**
     * 恢复短连接
     *
     * @param requestParam 恢复短连接请求参数
     */
    @Override
    public void recoverRecycleBinShortLink(ShortLinkRecoverRecycleBinReqDTO requestParam) {

        // 查询恢复短连接的 gid 时候已经被删除
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, requestParam.getGid());
        GroupDO groupDO = groupMapper.selectOne(queryWrapper);

        // 被删除就恢复到默认分组
        if (groupDO.getDelFlag() == 1) {
            LambdaQueryWrapper<GroupDO> qw = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getUsername, UserContext.getUsername())
                    .eq(GroupDO::getName, "默认分组")
                    .orderByAsc(GroupDO::getCreateTime)
                    .last("LIMIT 1");
            GroupDO defaultGroup = groupMapper.selectOne(qw);
            requestParam.setDefaultGid(defaultGroup.getGid());
        }
        shortLinkActualRemoteService.recoverRecycleBinShortLink(requestParam);
    }
}
