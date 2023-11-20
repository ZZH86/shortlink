package com.ch.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.shortlink.admin.common.convention.exception.ClientException;
import com.ch.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.ch.shortlink.admin.dao.entity.UserDO;
import com.ch.shortlink.admin.dao.mapper.UserMapper;
import com.ch.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.ch.shortlink.admin.dto.resp.UserRespDTO;
import com.ch.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import static com.ch.shortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;

/**
 * @Author hui cao
 * @Description: 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService{

    // 注入用户注册布隆过滤器
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    // redisson 分布式锁
    private final RedissonClient redissonClient;

    /**
     * 根据用户名返回用户
     */
    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if(userDO == null){
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
//        UserRespDTO result = new UserRespDTO();
        return BeanUtil.toBean(userDO, UserRespDTO.class);
    }

    /**
     * 查看用户名是否可用
     * @return true 代表未被使用，可以用，false 代表已被使用
     */
    @Override
    public Boolean hasUserName(String username) {
        return !userRegisterCachePenetrationBloomFilter.contains(username);
    }

    /**
     * 用户注册
     * @param requestParam 用户注册请求参数
     */
    @Override
    public void register(UserRegisterReqDTO requestParam) {

        // 名称已被使用则抛异常
        if(!hasUserName(requestParam.getUsername())){
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        }

        // 获得分布式锁
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParam.getUsername());

        try{
           if(lock.tryLock()) {
               int insert = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
               userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
               if(insert < 1){
                   throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);
               }
               return;
           }
           throw new ClientException(UserErrorCodeEnum.USER_EXIST);
        } finally {
            lock.unlock();
        }

    }

}
