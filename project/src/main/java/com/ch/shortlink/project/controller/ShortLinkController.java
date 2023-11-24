package com.ch.shortlink.project.controller;

import com.ch.shortlink.project.common.convention.result.Result;
import com.ch.shortlink.project.common.convention.result.Results;
import com.ch.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ch.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.ch.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author hui cao
 * @Description: 短链接控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 创建短链接
     * @return
     */
    @PostMapping("/api/short-link/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        return Results.success(shortLinkService.createShortLink(requestParam));
    }
}
