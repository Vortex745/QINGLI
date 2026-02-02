package com.jin.xianqu_backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jin.xianqu_backend.model.dto.LostFoundAddDTO;
import com.jin.xianqu_backend.model.entity.LostFound;
import com.jin.xianqu_backend.model.vo.LostFoundVO;

public interface LostFoundService extends IService<LostFound> {
    Boolean add(LostFoundAddDTO dto);

    Page<LostFoundVO> list(int page, int size, Integer type, Long userId, String keyword, String location,
            String sort);

    Page<LostFoundVO> listMy(int page, int size, String keyword);

    LostFoundVO getDetail(Long id);

    boolean incrementWantCount(Long id);
}
