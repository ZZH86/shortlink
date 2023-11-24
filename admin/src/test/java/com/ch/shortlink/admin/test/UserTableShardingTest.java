package com.ch.shortlink.admin.test;

/**
 * @Author hui cao
 * @Description:
 */
public class UserTableShardingTest {

    public final static String SQL = """
            CREATE TABLE `t_link_%d` (
              `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
              `domain` VARCHAR(128) DEFAULT NULL COMMENT '域名',
              `short_uri` VARCHAR(8) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '短链接',
              `full_short_url` VARCHAR(128) DEFAULT NULL COMMENT '完整短链接',
              `origin_url` VARCHAR(1024) DEFAULT NULL COMMENT '原始链接',
              `click_num` INT(11) DEFAULT '0' COMMENT '点击量',
              `gid` VARCHAR(32) DEFAULT 'default' COMMENT '分组标识',
              `enable_status` TINYINT(1) DEFAULT NULL COMMENT '启用标识 0：启用 1：未启用',
              `created_type` TINYINT(1) DEFAULT NULL COMMENT '创建类型 0：接口 1：控制台',
              `valid_date_type` TINYINT(1) DEFAULT NULL COMMENT '有效期类型 0：永久有效 1：用户自定义',
              `valid_date` DATETIME DEFAULT NULL COMMENT '有效期',
              `describe` VARCHAR(1024) DEFAULT NULL COMMENT '描述',
              `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
              `update_time` DATETIME DEFAULT NULL COMMENT '修改时间',
              `del_flag` TINYINT(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
              PRIMARY KEY (`id`),
              UNIQUE KEY `idx_unique_full_short_url` (`full_short_url`) USING BTREE
            ) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;""";

    public static void main(String[] args) {
        for (int i = 0; i < 16; i++) {
            System.out.printf((SQL) + "%n", i);
        }
    }
}
