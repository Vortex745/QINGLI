package com.jin.xianqu_admin.model.vo;

import lombok.Data;
import java.util.List;

/**
 * Dashboard statistics response VO
 * Platform focus: User Info Platform (no transactions/orders)
 */
@Data
public class DashboardStatsVO {

    // Core Metrics
    private Long totalUsers;
    private Long totalGoods;
    private Long totalPosts; // Combined: 校园广场 + 兼职发布 + 失物招领

    // Activity Metrics
    private Long todayNewUsers;
    private Long todayNewGoods;
    private Long todayNewPosts;

    // Growth Rates (percentage, e.g., 12 for 12%)
    private Integer userGrowth;
    private Integer goodsGrowth;
    private Integer postGrowth;

    // Trend Data (Last 7 days) - Post trend instead of order trend
    private List<DailyTrend> postTrend;

    // Category Distribution
    private List<CategoryStat> goodsCategoryStat;

    @Data
    public static class DailyTrend {
        private String date;
        private Long postCount;
    }

    @Data
    public static class CategoryStat {
        private String name;
        private Long value;
    }
}
