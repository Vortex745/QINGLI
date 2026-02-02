package com.jin.xianqu_backend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jin.xianqu_backend.common.Result;
import com.jin.xianqu_backend.model.vo.ReportVO;
import com.jin.xianqu_backend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 举报管理接口 (管理后台)
 */
@RestController
@RequestMapping("/admin/report")
public class AdminReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 获取举报列表
     */
    @GetMapping("/list")
    public Result<Page<ReportVO>> listReports(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer targetType) {
        return Result.success(reportService.listReports(page, size, status, targetType));
    }

    /**
     * 处理举报
     */
    @PostMapping("/handle")
    public Result<Boolean> handleReport(
            @RequestParam Long id,
            @RequestParam Integer status,
            @RequestParam(required = false, defaultValue = "") String handleResult) {
        return Result.success(reportService.handleReport(id, status, handleResult));
    }
}
