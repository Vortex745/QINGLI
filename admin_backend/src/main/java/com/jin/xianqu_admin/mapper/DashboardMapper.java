package com.jin.xianqu_admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * Mapper for dashboard statistics queries
 * Platform focus: User Info Platform (no transactions/orders)
 */
@Mapper
public interface DashboardMapper {

    @Select("SELECT COUNT(*) FROM sys_user")
    Long countTotalUsers();

    @Select("SELECT COUNT(*) FROM bus_goods WHERE is_delete = 0")
    Long countTotalGoods();

    /**
     * Total posts = bus_post (校园广场) + part_time_job (兼职发布) + lost_found (失物招领)
     */
    @Select("""
                SELECT
                    (SELECT COUNT(*) FROM bus_post WHERE is_delete = 0) +
                    (SELECT COUNT(*) FROM part_time_job) +
                    (SELECT COUNT(*) FROM bus_lost_found WHERE is_delete = 0)
                AS total
            """)
    Long countTotalPosts();

    @Select("SELECT COUNT(*) FROM sys_user WHERE DATE(create_time) = CURDATE()")
    Long countTodayNewUsers();

    @Select("SELECT COUNT(*) FROM bus_goods WHERE DATE(create_time) = CURDATE()")
    Long countTodayNewGoods();

    /**
     * Today new posts = new posts from all 3 categories
     */
    @Select("""
                SELECT
                    (SELECT COUNT(*) FROM bus_post WHERE is_delete = 0 AND DATE(create_time) = CURDATE()) +
                    (SELECT COUNT(*) FROM part_time_job WHERE DATE(create_time) = CURDATE()) +
                    (SELECT COUNT(*) FROM bus_lost_found WHERE is_delete = 0 AND DATE(create_time) = CURDATE())
                AS total
            """)
    Long countTodayNewPosts();

    /**
     * Get post counts for the last 7 days (combined from all post types)
     */
    @Select("""
                SELECT date_col as date, SUM(cnt) as postCount FROM (
                    SELECT DATE(create_time) as date_col, COUNT(*) as cnt FROM bus_post WHERE is_delete = 0 AND create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) GROUP BY DATE(create_time)
                    UNION ALL
                    SELECT DATE(create_time) as date_col, COUNT(*) as cnt FROM part_time_job WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) GROUP BY DATE(create_time)
                    UNION ALL
                    SELECT DATE(create_time) as date_col, COUNT(*) as cnt FROM bus_lost_found WHERE is_delete = 0 AND create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) GROUP BY DATE(create_time)
                ) combined
                GROUP BY date_col
                ORDER BY date_col ASC
            """)
    List<Map<String, Object>> getPostTrend();

    /**
     * Get goods count by category
     */
    @Select("""
                SELECT
                    COALESCE(category, '其他') as name,
                    COUNT(*) as value
                FROM bus_goods
                WHERE is_delete = 0
                GROUP BY category
            """)
    List<Map<String, Object>> getGoodsCategoryStat();
}
