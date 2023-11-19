package com.ch.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ch.shortlink.admin.dao.entity.UserDO;
import com.ch.shortlink.admin.dto.resp.UserRespDTO;

/**
 * @Author hui cao
 * @Description: 用户接口层
 */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户返回参数响应
     */
    UserRespDTO getUserByUsername(String username);
}
