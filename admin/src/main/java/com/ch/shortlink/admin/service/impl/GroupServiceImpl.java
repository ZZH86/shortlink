package com.ch.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.shortlink.admin.common.convention.exception.ClientException;
import com.ch.shortlink.admin.dao.entity.GroupDO;
import com.ch.shortlink.admin.dao.mapper.GroupMapper;
import com.ch.shortlink.admin.service.GroupService;
import com.ch.shortlink.admin.toolkit.RandomIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @Author hui cao
 * @Description: 短链接分组接口实现类
 */
@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService  {
    @Override
    public void saveGroup(String groupName) {
        String gid;
        while (true){
            gid = RandomIdGenerator.generateRandomId();
            LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getGid, gid)
                    // TODO 设置用户名
                    .eq(GroupDO::getUsername, null);
            GroupDO hasGroupFlag = baseMapper.selectOne(queryWrapper);
            if(hasGroupFlag == null){
                break;
            }
        }

        GroupDO groupDO = GroupDO.builder()
                .gid(RandomIdGenerator.generateRandomId())
                .sortOrder(0)
                .name(groupName)
                .build();
        int insert = baseMapper.insert(groupDO);
        if (insert < 1){
            throw new ClientException("短链接分组创建失败");
        }
    }
}
