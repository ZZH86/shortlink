package com.ch.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ch.shortlink.admin.common.convention.result.Result;
import com.ch.shortlink.admin.remote.ShortLinkRemoteService;
import com.ch.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.ch.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.ch.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.ch.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.ch.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author hui cao
 * @Description: 短链接控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    // TODO 后续重构为 springCloud Feign 调用
    ShortLinkRemoteService shortLinkService = new ShortLinkRemoteService(){};

    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        return shortLinkService.createShortLink(requestParam);
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>>  pageShortLink(ShortLinkPageReqDTO requestParam){
        return shortLinkService.pageShortLink(requestParam);
    }

    /**
     * 查询短链接分组内短链接数量
     * @return 分组数量列表
     */
    @GetMapping("/api/short-link/admin/v1/count")
    public Result<List<ShortLinkGroupCountQueryRespDTO>> linkGroupShortLinkCount(@RequestParam("requestParam") List<String> requestParam){
        return shortLinkService.linkGroupShortLinkCount(requestParam);
    }

}
