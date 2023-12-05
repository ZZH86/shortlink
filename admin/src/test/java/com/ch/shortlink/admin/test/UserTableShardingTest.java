package com.ch.shortlink.admin.test;

/**
 * @Author hui cao
 * @Description:
 */
public class UserTableShardingTest {

//    public final static String SQL = """
//            CREATE TABLE `t_group_%d` (
//              `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
//              `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',
//              `name` varchar(64) DEFAULT NULL COMMENT '分组名称',
//              `username` varchar(256) DEFAULT NULL COMMENT '创建分组用户名',
//              `sort_order` int(3) DEFAULT NULL COMMENT '分组排序',
//              `create_time` datetime DEFAULT NULL COMMENT '创建时间',
//              `update_time` datetime DEFAULT NULL COMMENT '修改时间',
//              `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
//              PRIMARY KEY (`id`),
//              UNIQUE KEY `idx_unique_username_gid` (`gid`,`username`) USING BTREE
//            ) ENGINE=InnoDB AUTO_INCREMENT=1726854951339364354 DEFAULT CHARSET=utf8mb4;""";

//    private final static String SQL = """
//            CREATE TABLE `t_link_%d` (
//              `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
//              `domain` VARCHAR(128) DEFAULT NULL COMMENT '域名',
//              `short_uri` VARCHAR(8) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '短链接',
//              `full_short_url` VARCHAR(128) DEFAULT NULL COMMENT '完整短链接',
//              `origin_url` VARCHAR(1024) DEFAULT NULL COMMENT '原始链接',
//              `click_num` INT(11) DEFAULT '0' COMMENT '点击量',
//              `gid` VARCHAR(32) DEFAULT 'default' COMMENT '分组标识',
//              `favicon` VARCHAR(256) DEFAULT NULL COMMENT '网站图标',
//              `enable_status` TINYINT(1) DEFAULT NULL COMMENT '启用标识 0：启用 1：未启用',
//              `created_type` TINYINT(1) DEFAULT NULL COMMENT '创建类型 0：接口 1：控制台',
//              `valid_date_type` TINYINT(1) DEFAULT NULL COMMENT '有效期类型 0：永久有效 1：用户自定义',
//              `valid_date` DATETIME DEFAULT NULL COMMENT '有效期',
//              `describe` VARCHAR(1024) DEFAULT NULL COMMENT '描述',
//              `total_pv` INT(11) DEFAULT NULL COMMENT '历史pv',
//              `total_uv` INT(11) DEFAULT NULL COMMENT '历史uv',
//              `total_uip` INT(11) DEFAULT NULL COMMENT '历史uip',
//              `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
//              `update_time` DATETIME DEFAULT NULL COMMENT '修改时间',
//              `del_flag` TINYINT(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
//              PRIMARY KEY (`id`),
//              UNIQUE KEY `idx_unique_full-short-url` (`full_short_url`) USING BTREE
//            ) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;""";

    private final static String SQL = """
            CREATE TABLE `t_link_stats_today_%d` (
              `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
              `gid` varchar(32) DEFAULT 'default' COMMENT '分组标识',
              `full_short_url` varchar(128) DEFAULT NULL COMMENT '短链接',
              `date` date DEFAULT NULL COMMENT '日期',
              `today_pv` int(11) DEFAULT '0' COMMENT '今日PV',
              `today_uv` int(11) DEFAULT '0' COMMENT '今日UV',
              `today_uip` int(11) DEFAULT '0' COMMENT '今日IP数',
              `create_time` datetime DEFAULT NULL COMMENT '创建时间',
              `update_time` datetime DEFAULT NULL COMMENT '修改时间',
              `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
              PRIMARY KEY (`id`),
              UNIQUE KEY `idx_unique_full-short-url` (`full_short_url`) USING BTREE
            ) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;;""";

    public static void main(String[] args) {
        for (int i = 0; i < 16; i++) {
            System.out.printf((SQL) + "%n", i);
        }
    }
}
