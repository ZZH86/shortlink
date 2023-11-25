package com.ch.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ch.shortlink.admin.dao.entity.GroupDO;
import com.ch.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.ch.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.ch.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;

import java.util.List;

/**
 * @Author hui cao
 * @Description: 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 新增短链接分组
     *
     * @param groupName 短链接分组名
     */
    void saveGroup(String groupName);

    /**
     * 新增短链接分组（提供用户名）
     *
     * @param username  用户名
     * @param groupName 分组名
     */
    void saveGroup(String username, String groupName);

    /**
     * 查询用户短链接分组集合
     *
     * @return 用户短链接分组集合
     */
    List<ShortLinkGroupRespDTO> listGroup();

    /**
     * 修改短链接分组名称
     *
     * @param requestParam 修改名称请求参数
     */
    void updateGroup(ShortLinkGroupUpdateReqDTO requestParam);

    /**
     * 删除短链接分组
     *
     * @param gid 分组标识
     */
    void deleteGroup(String gid);

    /**
     * 短链接分组排序功能
     *
     * @param requestParam 分组排序请求参数
     */
    void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam);
}
