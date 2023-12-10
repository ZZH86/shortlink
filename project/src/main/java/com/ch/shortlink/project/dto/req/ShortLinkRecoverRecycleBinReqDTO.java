package com.ch.shortlink.project.dto.req;

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
     * 分组标识
     */
    private String defaultGid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}
