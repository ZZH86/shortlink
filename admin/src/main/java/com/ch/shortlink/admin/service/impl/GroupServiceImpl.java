package com.ch.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.shortlink.admin.common.biz.user.UserContext;
import com.ch.shortlink.admin.common.convention.exception.ClientException;
import com.ch.shortlink.admin.dao.entity.GroupDO;
import com.ch.shortlink.admin.dao.mapper.GroupMapper;
import com.ch.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.ch.shortlink.admin.service.GroupService;
import com.ch.shortlink.admin.toolkit.RandomIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author hui cao
 * @Description: 短链接分组接口实现类
 */
@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    /**
     * 新增短链接分组
     *
     * @param groupName 短链接分组名
     */
    @Override
    public void saveGroup(String groupName) {
        String gid;
        while (true) {
            gid = RandomIdGenerator.generateRandomId();
            // 根据用户名和 gid 去查询数据库，如果有就重新生成gid
            LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getGid, gid)
                    .eq(GroupDO::getUsername, UserContext.getUsername());
            GroupDO hasGroupFlag = baseMapper.selectOne(queryWrapper);
            if (hasGroupFlag == null) {
                break;
            }
        }
        // 创建短链接分组
        GroupDO groupDO = GroupDO.builder()
                .gid(gid)
                .sortOrder(0)
                .username(UserContext.getUsername())
                .name(groupName)
                .build();
        int insert = baseMapper.insert(groupDO);
        if (insert < 1) {
            throw new ClientException("短链接分组创建失败");
        }
    }

    /**
     * 查询用户短链接分组集合
     */
    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);
        List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);
        return BeanUtil.copyToList(groupDOList, ShortLinkGroupRespDTO.class);
    }
}
