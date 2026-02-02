package com.jin.xianqu_admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jin.xianqu_admin.mapper.LostFoundMapper;
import com.jin.xianqu_admin.model.dto.LostFoundPageDTO;
import com.jin.xianqu_admin.model.entity.LostFound;
import com.jin.xianqu_admin.service.LostFoundService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LostFoundServiceImpl extends ServiceImpl<LostFoundMapper, LostFound> implements LostFoundService {

    @Override
    public Page<LostFound> getLostFoundList(LostFoundPageDTO dto) {
        LambdaQueryWrapper<LostFound> wrapper = new LambdaQueryWrapper<>();

        // 按类型筛选
        if (dto.getType() != null) {
            wrapper.eq(LostFound::getType, dto.getType());
        }

        // 按物品名称模糊搜索
        if (StringUtils.hasText(dto.getItemName())) {
            wrapper.like(LostFound::getItemName, dto.getItemName());
        }

        // 按状态筛选
        if (dto.getStatus() != null) {
            wrapper.eq(LostFound::getStatus, dto.getStatus());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(LostFound::getCreateTime);

        Page<LostFound> page = new Page<>(dto.getPage(), dto.getSize());
        return this.page(page, wrapper);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        LostFound lostFound = this.getById(id);
        if (lostFound != null) {
            lostFound.setStatus(status);
            this.updateById(lostFound);
        }
    }

    @Override
    public void deleteLostFound(Long id) {
        this.removeById(id);
    }
}
