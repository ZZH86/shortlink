package com.ch.shortlink.admin.remote.dto.req;

import lombok.Data;

/**
 * @Author hui cao
 * @Description: 恢复回收站短链接请求对象
 */
@Data
public class ShortLinkRecoverRecycleBinReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}
