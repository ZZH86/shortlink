package com.ch.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ch.shortlink.admin.dao.entity.GroupDO;

/**
 * @Author hui cao
 * @Description: 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 新增短链接分组
     * @param groupName 短链接分组名
     */
    void saveGroup(String groupName);
}
