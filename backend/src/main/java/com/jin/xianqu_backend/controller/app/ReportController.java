package com.jin.xianqu_backend.controller.app;

import com.jin.xianqu_backend.common.Result;
import com.jin.xianqu_backend.model.dto.ReportAddDTO;
import com.jin.xianqu_backend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * 举报接口 (APP端)
 */
@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 提交举报
     */
    @PostMapping("/submit")
    public Result<Boolean> submitReport(@RequestBody @Valid ReportAddDTO dto) {
        return Result.success(reportService.submitReport(dto));
    }
}
