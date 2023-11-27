package com.ch.shortlink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ch.shortlink.admin.common.biz.user.UserContext;
import com.ch.shortlink.admin.common.convention.result.Result;
import com.ch.shortlink.admin.dao.entity.GroupDO;
import com.ch.shortlink.admin.dao.mapper.GroupMapper;
import com.ch.shortlink.admin.remote.ShortLinkRemoteService;
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

    // TODO 后续重构为 springCloud Feign 调用
    ShortLinkRemoteService shortLinkService = new ShortLinkRemoteService() {
    };

    private final GroupMapper groupMapper;

    /**
     * 分页查询回收站
     * @param requestParam 分页查询回收站请求参数
     * @return 分页查询回收站返回对象
     */
    @Override
    public Result<IPage<ShortLinkPageRespDTO>> pageRecycleShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        List<GroupDO> groupDOList = groupMapper.selectList(queryWrapper);
        if(CollUtil.isEmpty(groupDOList)){
            throw new SecurityException("用户无分组信息");
        }
        requestParam.setGidList(groupDOList.stream().map(GroupDO::getGid).toList());
        return shortLinkService.pageRecycleShortLink(requestParam);
    }
}
