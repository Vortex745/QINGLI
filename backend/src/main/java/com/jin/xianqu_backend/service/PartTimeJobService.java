package com.jin.xianqu_backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jin.xianqu_backend.model.dto.PartTimeJobAddDTO;
import com.jin.xianqu_backend.model.entity.PartTimeJob;
import com.jin.xianqu_backend.model.vo.PartTimeJobVO;

public interface PartTimeJobService extends IService<PartTimeJob> {
    boolean addJob(PartTimeJobAddDTO dto);

    Page<PartTimeJobVO> listJobs(int page, int size);

    Page<PartTimeJobVO> listJobs(int page, int size, String sort, Long userId, String keyword, String location);

    PartTimeJobVO getJobDetail(Long id);

    PartTimeJobVO getJobForEdit(Long id);

    Page<PartTimeJobVO> listMyJobs(int page, int size, String keyword);

    boolean incrementWantCount(Long id);
}
