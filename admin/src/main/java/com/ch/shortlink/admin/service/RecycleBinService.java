package com.ch.shortlink.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ch.shortlink.admin.common.convention.result.Result;
import com.ch.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.ch.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;

/**
 * @Author hui cao
 * @Description: URL 回收站接口层
 */
public interface RecycleBinService {

    /**
     * 分页查询回收站
     * @param requestParam 分页查询回收站请求参数
     * @return 分页查询回收站返回对象
     */
    Result<IPage<ShortLinkPageRespDTO>> pageRecycleShortLink(ShortLinkRecycleBinPageReqDTO requestParam);
}
