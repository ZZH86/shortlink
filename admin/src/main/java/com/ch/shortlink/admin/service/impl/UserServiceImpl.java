package com.ch.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch.shortlink.admin.common.convention.exception.ClientException;
import com.ch.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.ch.shortlink.admin.dao.entity.UserDO;
import com.ch.shortlink.admin.dao.mapper.UserMapper;
import com.ch.shortlink.admin.dto.resp.UserRespDTO;
import com.ch.shortlink.admin.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @Author hui cao
 * @Description: 用户接口实现层
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService{

    /**
     * 根据用户名返回用户
     * @param username 用户名
     * @return
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

}
