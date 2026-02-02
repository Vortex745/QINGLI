package com.jin.xianqu_admin.service;

import com.jin.xianqu_admin.model.vo.DashboardStatsVO;

public interface DashboardService {

    /**
     * Get all dashboard statistics
     */
    DashboardStatsVO getStats();
}
