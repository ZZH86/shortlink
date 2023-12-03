package com.ch.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ch.shortlink.project.dao.entity.LinkLocalStatsDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * @Author hui cao
 * @Description: 地区访问实体持久层
 */
public interface LinkLocalStatsMapper extends BaseMapper<LinkLocalStatsDO> {

    /**
     * 记录地区访问监控数据
     */
    @Insert("INSERT INTO t_link_local_stats (full_short_url, gid, date, cnt, country, province, city, adcode, create_time, update_time, del_flag) " +
            "VALUES( #{linkLocalStats.fullShortUrl}, #{linkLocalStats.gid}, #{linkLocalStats.date}, #{linkLocalStats.cnt}, #{linkLocalStats.country}, #{linkLocalStats.province}, #{linkLocalStats.city}, #{linkLocalStats.adcode}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt +  #{linkLocalStats.cnt};")
    void shortLinkLocaleState(@Param("linkLocalStats") LinkLocalStatsDO linkLocalStatsDO);

}
