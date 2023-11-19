package com.ch.shortlink.admin.controller;

import com.ch.shortlink.admin.common.convention.result.Result;
import com.ch.shortlink.admin.common.convention.result.Results;
import com.ch.shortlink.admin.dto.resp.UserRespDTO;
import com.ch.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author hui cao
 * @Description: 用户管理控制层
 */

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("api/shortlink/v1/user/{username}")
    public Result<UserRespDTO> getUserByUserName(@PathVariable("username") String username){
        UserRespDTO userRespDTO = userService.getUserByUsername(username);
        return Results.success(userRespDTO);
    }


}
