package com.ch.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.shortlink.admin.common.biz.user.UserContext;
import com.ch.shortlink.admin.common.convention.exception.ClientException;
import com.ch.shortlink.admin.dao.entity.GroupDO;
import com.ch.shortlink.admin.dao.mapper.GroupMapper;
import com.ch.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.ch.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.ch.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.ch.shortlink.admin.remote.ShortLinkRemoteService;
import com.ch.shortlink.admin.remote.dto.req.ShortLinkSaveBatchRecycleBinReqDTO;
import com.ch.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.ch.shortlink.admin.service.GroupService;
import com.ch.shortlink.admin.service.RecycleBinService;
import com.ch.shortlink.admin.toolkit.RandomIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author hui cao
 * @Description: 短链接分组接口实现类
 */
@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    // TODO 后续重构为 springCloud Feign 调用
    ShortLinkRemoteService shortLinkService = new ShortLinkRemoteService() {
    };

    private final RecycleBinService recycleBinService;

    /**
     * 新增短链接分组
     *
     * @param groupName 短链接分组名
     */
    @Override
    public void saveGroup(String groupName) {
        saveGroup(UserContext.getUsername(), groupName);
    }

    @Override
    public void saveGroup(String username, String groupName) {
        String gid;
        while (true) {
            gid = RandomIdGenerator.generateRandomId();
            // 根据用户名和 gid 去查询数据库，如果有就重新生成gid
            LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getGid, gid)
                    .eq(GroupDO::getUsername, username);
            GroupDO hasGroupFlag = baseMapper.selectOne(queryWrapper);
            if (hasGroupFlag == null) {
                break;
            }
        }
        // 创建短链接分组
        GroupDO groupDO = GroupDO.builder()
                .gid(gid)
                .sortOrder(0)
                .username(username)
                .name(groupName)
                .build();
        int insert = baseMapper.insert(groupDO);
        if (insert < 1) {
            throw new ClientException("短链接分组创建失败");
        }
    }

    /**
     * 查询用户短链接分组集合
     * 获取用户所属的分组信息，并返回包含每个分组链接数量的分组信息列表
     */
    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);

        // 查询当前用户的所有分组标识
        List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);

        // 传入用户的分组标识来查询分组列表下短链接数量
        List<ShortLinkGroupCountQueryRespDTO> countQueryRespDTOS =
                shortLinkService.linkGroupShortLinkCount(groupDOList.stream().map(GroupDO::getGid).toList()).getData();

        // 类型转换
        List<ShortLinkGroupRespDTO> results = BeanUtil.copyToList(groupDOList, ShortLinkGroupRespDTO.class);

        // 将 ShortLinkGroupCountQueryRespDTO 结果转换成 map 集合
        Map<String, Integer> counts = countQueryRespDTOS.stream().collect(Collectors.toMap(ShortLinkGroupCountQueryRespDTO::getGid, ShortLinkGroupCountQueryRespDTO::getShortLinkCount));

        // 将 map 的 count 结果设置进返回对象
        return results.stream().peek(result -> result.setShortLinkCount((counts.get(result.getGid()) == null) ? 0 : counts.get(result.getGid()))).toList();
    }

    /**
     * 修改短链接分组名称
     *
     * @param requestParam 修改名称请求参数
     */
    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO requestParam) {
        LambdaUpdateWrapper<GroupDO> queryWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, requestParam.getGid())
                .eq(GroupDO::getDelFlag, 0);
        GroupDO groupDO1 = baseMapper.selectOne(queryWrapper);
        if(Objects.equals(groupDO1.getName(), "默认分组")){
            throw new ClientException("默认分组不可修改");
        }
        GroupDO groupDO = new GroupDO();
        groupDO.setName(requestParam.getName());
        int update = baseMapper.update(groupDO, queryWrapper);
        if (update < 1) {
            throw new ClientException("分组信息更改失败");
        }
    }

    /**
     * 删除短链接分组
     *
     * @param gid 分组标识
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(String gid) {
        // 默认分组，不可删除
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getDelFlag, 0);
        GroupDO groupDO1 = baseMapper.selectOne(queryWrapper);
        if(Objects.equals(groupDO1.getName(), "默认分组")){
            throw new ClientException("默认分组不可删除");
        }

        // 删除分组前应该将所有的短链接放到回收站
        ShortLinkSaveBatchRecycleBinReqDTO shortLinkSaveBatchRecycleBinReqDTO = ShortLinkSaveBatchRecycleBinReqDTO.builder()
                .gid(gid).build();
        shortLinkService.saveBatchRecycleBin(shortLinkSaveBatchRecycleBinReqDTO);

        // 删除分组
        GroupDO groupDO = new GroupDO();
        groupDO.setDelFlag(1);
        int update = baseMapper.update(groupDO, queryWrapper);
        if (update < 1) {
            throw new ClientException("分组信息删除失败");
        }
    }

    /**
     * @param requestParam 分组排序请求参数
     */
    @Override
    public void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam) {
        requestParam.forEach(each -> {
            GroupDO groupDO = GroupDO.builder()
                    .sortOrder(each.getSortOrder())
                    .build();
            LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                    .eq(GroupDO::getUsername, UserContext.getUsername())
                    .eq(GroupDO::getGid, each.getGid())
                    .eq(GroupDO::getDelFlag, 0);
            baseMapper.update(groupDO, updateWrapper);
        });
    }
}
