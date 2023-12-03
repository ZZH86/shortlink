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
 * @Description: 短链接访问设备实体
 */
@Data
@TableName("t_link_device_stats")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LinkDeviceStatsDO extends BaseDO {

    /**
     * id
     */
    private Long id;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 日期
     */
    private Date date;

    /**
     * 访问量
     */
    private Integer cnt;

    /**
     * 访问设备
     */
    private String device;
}
