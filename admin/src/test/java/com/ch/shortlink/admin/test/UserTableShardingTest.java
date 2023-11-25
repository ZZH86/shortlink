package com.ch.shortlink.admin.test;

/**
 * @Author hui cao
 * @Description:
 */
public class UserTableShardingTest {

    public final static String SQL = """
            CREATE TABLE `t_group_%d` (
              `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
              `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',
              `name` varchar(64) DEFAULT NULL COMMENT '分组名称',
              `username` varchar(256) DEFAULT NULL COMMENT '创建分组用户名',
              `sort_order` int(3) DEFAULT NULL COMMENT '分组排序',
              `create_time` datetime DEFAULT NULL COMMENT '创建时间',
              `update_time` datetime DEFAULT NULL COMMENT '修改时间',
              `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
              PRIMARY KEY (`id`),
              UNIQUE KEY `idx_unique_username_gid` (`gid`,`username`) USING BTREE
            ) ENGINE=InnoDB AUTO_INCREMENT=1726854951339364354 DEFAULT CHARSET=utf8mb4;""";

    public static void main(String[] args) {
        for (int i = 0; i < 16; i++) {
            System.out.printf((SQL) + "%n", i);
        }
    }
}
