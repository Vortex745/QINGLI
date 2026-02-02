package com.jin.xianqu_admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jin.xianqu_admin.model.dto.LostFoundPageDTO;
import com.jin.xianqu_admin.model.entity.LostFound;

public interface LostFoundService extends IService<LostFound> {

    Page<LostFound> getLostFoundList(LostFoundPageDTO dto);

    void updateStatus(Long id, Integer status);

    void deleteLostFound(Long id);
}
