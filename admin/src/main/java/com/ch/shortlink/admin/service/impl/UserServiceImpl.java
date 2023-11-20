package com.ch.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.shortlink.admin.common.convention.exception.ClientException;
import com.ch.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.ch.shortlink.admin.dao.entity.UserDO;
import com.ch.shortlink.admin.dao.mapper.UserMapper;
import com.ch.shortlink.admin.dto.req.UserLoginReqDTO;
import com.ch.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.ch.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.ch.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.ch.shortlink.admin.dto.resp.UserRespDTO;
import com.ch.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.ch.shortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;

/**
 * @Author hui cao
 * @Description: 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    // 注入用户注册布隆过滤器
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    // redisson 分布式锁
    private final RedissonClient redissonClient;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 根据用户名返回用户
     */
    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        return BeanUtil.toBean(userDO, UserRespDTO.class);
    }

    /**
     * 查看用户名是否可用
     *
     * @return true 代表未被使用，可以用，false 代表已被使用
     */
    @Override
    public Boolean hasUserName(String username) {
        return !userRegisterCachePenetrationBloomFilter.contains(username);
    }

    /**
     * 用户注册
     *
     * @param requestParam 用户注册请求参数
     */
    @Override
    public void register(UserRegisterReqDTO requestParam) {

        // 名称已被使用则抛异常
        if (!hasUserName(requestParam.getUsername())) {
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        }

        // 获得分布式锁
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParam.getUsername());

        try {
            if (lock.tryLock()) {
                int insert = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
                userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
                if (insert < 1) {
                    throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);
                }
                return;
            }
            throw new ClientException(UserErrorCodeEnum.USER_EXIST);
        } finally {
            lock.unlock();
        }

    }

    /**
     * 根据用户名修改用户信息
     *
     * @param requestParam 修改用户请求参数
     */
    @Override
    public void update(UserUpdateReqDTO requestParam) {
        // TODO 验证当前用户名是否为登录用户
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername());
        baseMapper.update(BeanUtil.toBean(requestParam, UserDO.class), queryWrapper);
    }

    /**
     * 用户登录
     *
     * @param requestParam 用户登录请求参数
     * @return 用户登录返回响应 token
     */
    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        // 判断用户名和密码是否一样
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername())
                .eq(UserDO::getPassword, requestParam.getPassword())
                .eq(UserDO::getDelFlag, 0);
        UserDO userDO = baseMapper.selectOne(queryWrapper);

        // 没有该用户抛出异常
        if (userDO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }

        Boolean hasLogin = stringRedisTemplate.hasKey("login_" + requestParam.getUsername());
        if (hasLogin != null && hasLogin) {
            throw new ClientException(UserErrorCodeEnum.USER_LOGIN_EXIST);
        }
        // 生成 token

        /**
         * Hash
         * Key: login_用户名
         * Value:
         *   key: token 标识
         *   Val: JSON 字符串(用户信息)
         */
        String uuid = UUID.randomUUID().toString();
        // 用 token 作为 key 将用户信息存储到 redis, 并设置过期时间 30 分钟
        stringRedisTemplate.opsForHash().put("login_" + requestParam.getUsername(), uuid, JSON.toJSONString(userDO));
        stringRedisTemplate.expire("login_" + requestParam.getUsername(), 30L, TimeUnit.MINUTES);

        return new UserLoginRespDTO(uuid);
    }

    /**
     * 检查用户是否登录
     *
     * @param token 用户登录 Token
     * @return 是否登录标识
     */
    @Override
    public Boolean checkLogin(String username, String token) {
        return stringRedisTemplate.opsForHash().get("login_" + username, token) != null;
    }

    @Override
    public void logout(String username, String token) {
        if (checkLogin(username, token)) {
            stringRedisTemplate.delete("login_" + username);
            return;
        } else {
            throw new ClientException("用户token过期或者用户名不存在");
        }
    }

}
