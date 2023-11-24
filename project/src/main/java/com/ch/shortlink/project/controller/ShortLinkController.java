package com.ch.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ch.shortlink.project.common.convention.result.Result;
import com.ch.shortlink.project.common.convention.result.Results;
import com.ch.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ch.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ch.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.ch.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.ch.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
     */
    @PostMapping("/api/short-link/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        return Results.success(shortLinkService.createShortLink(requestParam));
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>>  pageShortLink(ShortLinkPageReqDTO requestParam){
        return Results.success(shortLinkService.pageShortLink(requestParam));
    }

}
