package com.ch.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ch.shortlink.admin.dao.entity.GroupDO;
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
}
