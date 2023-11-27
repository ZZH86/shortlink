package com.ch.shortlink.admin.remote.dto.req;

import lombok.Data;

/**
 * @Author hui cao
 * @Description: 移至回收站请求对象
 */
@Data
public class ShortLinkSaveRecycleBinReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}
