package com.jin.xianqu_admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jin.xianqu_admin.common.Result;
import com.jin.xianqu_admin.model.dto.PageRequestDTO;

import com.jin.xianqu_admin.model.vo.ReportVO;
import com.jin.xianqu_admin.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping("/list")
    public Result<Page<ReportVO>> getList(@RequestBody PageRequestDTO queryDTO) {
        return Result.success(reportService.getList(queryDTO));
    }

    @PutMapping("/{id}/handle")
    public Result<String> handleReport(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String remark) {
        reportService.handleReport(id, status, remark);
        return Result.success("处理成功");
    }
}
