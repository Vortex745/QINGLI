package com.jin.xianqu_backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jin.xianqu_backend.model.dto.ReportAddDTO;
import com.jin.xianqu_backend.model.entity.Report;
import com.jin.xianqu_backend.model.vo.ReportVO;

public interface ReportService extends IService<Report> {

    /**
     * 提交举报
     */
    Boolean submitReport(ReportAddDTO dto);

    /**
     * 获取举报列表 (管理后台)
     */
    Page<ReportVO> listReports(int page, int size, Integer status, Integer targetType);

    /**
     * 处理举报
     */
    Boolean handleReport(Long id, Integer status, String handleResult);
}
