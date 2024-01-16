package com.ch.shortlink.admin.common.constant;

/**
 * @Author hui cao
 * @Description: 短链接后管 redis 缓存常量类
 */
public class RedisCacheConstant {

    /**
     * 用户登录缓存标识
     */
    public static final String USER_LOGIN_KEY = "short-link:login";

    /**
     * 用户注册分布式锁
     */
    public static final String LOCK_USER_REGISTER_KEY = "short-link:lock_user-register:%s";

    /**
     * 用户创建分组分布式锁
     */
    public static final String LOCK_GROUP_CREATE_KEY = "short-link:lock_group-create:%s";
}
