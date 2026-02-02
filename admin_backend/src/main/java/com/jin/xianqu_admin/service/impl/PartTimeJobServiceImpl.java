package com.jin.xianqu_admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jin.xianqu_admin.exception.BusinessException;
import com.jin.xianqu_admin.mapper.PartTimeJobMapper;
import com.jin.xianqu_admin.model.dto.PageRequestDTO;
import com.jin.xianqu_admin.model.entity.PartTimeJob;
import com.jin.xianqu_admin.service.PartTimeJobService;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

@Service
public class PartTimeJobServiceImpl extends ServiceImpl<PartTimeJobMapper, PartTimeJob> implements PartTimeJobService {

    @Override
    public Page<PartTimeJob> getList(PageRequestDTO queryDTO) {
        Page<PartTimeJob> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        QueryWrapper<PartTimeJob> queryWrapper = new QueryWrapper<>();

        // Basic filtering if needed, e.g. search by title
        if (StringUtils.isNotBlank(queryDTO.getKeyword())) {
            queryWrapper.like("title", queryDTO.getKeyword())
                    .or().like("content", queryDTO.getKeyword());
        }

        queryWrapper.orderByDesc("create_time");
        return this.page(page, queryWrapper);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        PartTimeJob job = this.getById(id);
        if (job == null) {
            throw new BusinessException(404, "兼职不存在");
        }
        job.setStatus(status);
        this.updateById(job);
    }
}
