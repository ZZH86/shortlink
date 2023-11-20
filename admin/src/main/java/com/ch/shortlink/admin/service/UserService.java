package com.ch.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ch.shortlink.admin.dao.entity.UserDO;
import com.ch.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.ch.shortlink.admin.dto.resp.UserRespDTO;

/**
 * @Author hui cao
 * @Description: 用户接口层
 */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户返回参数响应
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 查询用户名是否可用
     *
     * @param username 用户名
     * @return 用户名存在 False, 不存在返回 True
     */
    Boolean hasUserName(String username);

    /**
     * 用户注册
     * @param requestParam 用户注册请求参数
     */
    void register(UserRegisterReqDTO requestParam);

}
