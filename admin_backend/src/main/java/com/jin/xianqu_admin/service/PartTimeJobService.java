package com.jin.xianqu_admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jin.xianqu_admin.model.dto.PageRequestDTO;
import com.jin.xianqu_admin.model.entity.PartTimeJob;

public interface PartTimeJobService extends IService<PartTimeJob> {
    Page<PartTimeJob> getList(PageRequestDTO queryDTO);

    void updateStatus(Long id, Integer status);
}
