package com.jin.xianqu_admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jin.xianqu_admin.model.dto.PageRequestDTO;
import com.jin.xianqu_admin.model.entity.Report;
import com.jin.xianqu_admin.model.vo.ReportVO;

public interface ReportService extends IService<Report> {
    Page<ReportVO> getList(PageRequestDTO queryDTO);

    void handleReport(Long id, Integer status, String remark);
}
