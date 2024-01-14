package com.ch.shortlink.project.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.ch.shortlink.project.common.convention.result.Result;
import com.ch.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ch.shortlink.project.dto.resp.ShortLinkCreateRespDTO;

/**
 * @Author hui cao
 * @Description: 自定义流控规则
 */
public class CustomBlockHandler {

    public static Result<ShortLinkCreateRespDTO> createShortLinkBlockHandlerMethod(ShortLinkCreateReqDTO requestParam, BlockException e){
        return new Result<ShortLinkCreateRespDTO>().setCode("B100000").setMessage("当前网站访问人数较多，请稍后再试......");
    }
}
