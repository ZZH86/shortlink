package com.ch.shortlink.admin.dto.req;

import lombok.Data;

/**
 * @Author hui cao
 * @Description: 短链接分组请求参数
 */
@Data
public class ShortLinkGroupSortReqDTO {

    /**
     * 分组ID
     */
    private String gid;

    /**
     * 排序参数
     */
    private Integer sortOrder;

}
