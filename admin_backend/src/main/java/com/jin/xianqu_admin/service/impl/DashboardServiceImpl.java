package com.jin.xianqu_admin.service.impl;

import com.jin.xianqu_admin.mapper.DashboardMapper;
import com.jin.xianqu_admin.model.vo.DashboardStatsVO;
import com.jin.xianqu_admin.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dashboard Service Implementation
 * Platform focus: User Info Platform (no transactions/orders)
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardMapper dashboardMapper;

    @Override
    public DashboardStatsVO getStats() {
        DashboardStatsVO stats = new DashboardStatsVO();

        // Core Metrics
        stats.setTotalUsers(dashboardMapper.countTotalUsers());
        stats.setTotalGoods(dashboardMapper.countTotalGoods());
        stats.setTotalPosts(dashboardMapper.countTotalPosts()); // Combined posts

        // Activity Metrics
        stats.setTodayNewUsers(dashboardMapper.countTodayNewUsers());
        stats.setTodayNewGoods(dashboardMapper.countTodayNewGoods());
        stats.setTodayNewPosts(dashboardMapper.countTodayNewPosts());

        // Calculate Growth Rates
        stats.setUserGrowth(calculateGrowth(stats.getTotalUsers(), stats.getTodayNewUsers()));
        stats.setGoodsGrowth(calculateGrowth(stats.getTotalGoods(), stats.getTodayNewGoods()));
        stats.setPostGrowth(calculateGrowth(stats.getTotalPosts(), stats.getTodayNewPosts()));

        // Post Trend (Last 7 days)
        List<Map<String, Object>> rawTrend = dashboardMapper.getPostTrend();
        Map<String, Map<String, Object>> trendMap = rawTrend.stream()
                .collect(Collectors.toMap(
                        m -> m.get("date").toString(),
                        m -> m));

        // Fill in missing days with zeros
        List<DashboardStatsVO.DailyTrend> trendList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(formatter);

            DashboardStatsVO.DailyTrend trend = new DashboardStatsVO.DailyTrend();
            trend.setDate(date.format(DateTimeFormatter.ofPattern("MM-dd")));

            if (trendMap.containsKey(dateStr)) {
                Map<String, Object> dayData = trendMap.get(dateStr);
                trend.setPostCount(((Number) dayData.get("postCount")).longValue());
            } else {
                trend.setPostCount(0L);
            }
            trendList.add(trend);
        }
        stats.setPostTrend(trendList);

        // Category Statistics
        List<Map<String, Object>> rawCategory = dashboardMapper.getGoodsCategoryStat();
        List<DashboardStatsVO.CategoryStat> categoryList = rawCategory.stream()
                .map(m -> {
                    DashboardStatsVO.CategoryStat cat = new DashboardStatsVO.CategoryStat();
                    cat.setName((String) m.get("name"));
                    cat.setValue(((Number) m.get("value")).longValue());
                    return cat;
                })
                .collect(Collectors.toList());
        stats.setGoodsCategoryStat(categoryList);

        return stats;
    }

    private Integer calculateGrowth(Long total, Long today) {
        if (total == null || total == 0)
            return 0;
        if (today == null || today == 0)
            return 0;
        long previous = total - today;
        if (previous <= 0)
            return 100;
        return (int) (((double) today / previous) * 100);
    }
}
