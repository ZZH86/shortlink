package com.ch.shortlink.project.dto.req;

import lombok.Data;

/**
 * @Author hui cao
 * @Description: 分组短链接监控请求参数
 */
@Data
public class ShortLinkGroupStatsReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;
}