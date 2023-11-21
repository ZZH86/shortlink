package com.ch.shortlink.admin.dto.req;

import lombok.Data;

/**
 * @Author hui cao
 * @Description: 短链接分组修改请求参数
 */
@Data
public class ShortLinkGroupUpdateReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名
     */
    private String name;
}
