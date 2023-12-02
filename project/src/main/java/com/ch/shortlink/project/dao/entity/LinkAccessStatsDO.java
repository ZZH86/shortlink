package com.ch.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ch.shortlink.project.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author hui cao
 * @Description: 短链接基础访问监控实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_link_access_stats")
public class LinkAccessStatsDO extends BaseDO {

    /**
     * id
     */
    private Long id;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 日期
     */
    private Date date;

    /**
     * 访问量
     */
    private Integer pv;

    /**
     * 独立访问数
     */
    private Integer uv;

    /**
     * 独立ip数
     */
    private Integer uip;

    /**
     * 小时
     */
    private Integer hour;

    /**
     * 星期
     */
    private Integer weekday;

    /**
     * 删除标识：0 未删除 1 已删除
     */
    private Integer delFlag;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 状态
     */
    private Integer status;
}
