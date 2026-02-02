package com.jin.xianqu_admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jin.xianqu_admin.model.dto.PageRequestDTO;
import com.jin.xianqu_admin.model.entity.Notice;

public interface NoticeService extends IService<Notice> {
    Page<Notice> getList(PageRequestDTO queryDTO);

    void sendNotice(Notice notice);
}
