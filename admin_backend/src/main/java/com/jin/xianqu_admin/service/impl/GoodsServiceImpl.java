package com.jin.xianqu_admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jin.xianqu_admin.exception.BusinessException;
import com.jin.xianqu_admin.mapper.GoodsMapper;
import com.jin.xianqu_admin.model.dto.GoodsPageDTO;
import com.jin.xianqu_admin.model.entity.Goods;
import com.jin.xianqu_admin.service.GoodsService;
import com.jin.xianqu_admin.mapper.UserMapper;
import com.jin.xianqu_admin.model.entity.User;
import com.jin.xianqu_admin.model.vo.GoodsVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Page<GoodsVO> getGoodsList(GoodsPageDTO dto) {
        Page<Goods> page = new Page<>(dto.getPage(), dto.getSize());

        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotBlank(dto.getName())) {
            queryWrapper.like("name", dto.getName());
        }

        if (StringUtils.isNotBlank(dto.getCategory())) {
            queryWrapper.eq("category", dto.getCategory());
        }

        if (dto.getStatus() != null) {
            queryWrapper.eq("status", dto.getStatus());
        }

        queryWrapper.orderByDesc("create_time");

        Page<Goods> goodsPage = this.page(page, queryWrapper);

        // Convert to VO
        Page<GoodsVO> voPage = new Page<>(goodsPage.getCurrent(), goodsPage.getSize(), goodsPage.getTotal());
        List<Goods> records = goodsPage.getRecords();
        if (records.isEmpty()) {
            return voPage;
        }

        // Batch fetch users
        Set<Long> userIds = records.stream().map(Goods::getUserId).collect(Collectors.toSet());
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        List<GoodsVO> voList = records.stream().map(goods -> {
            GoodsVO vo = new GoodsVO();
            if (goods != null) {
                BeanUtils.copyProperties(goods, vo);
            }

            // Fill in user information
            User seller = (goods != null) ? userMap.get(goods.getUserId()) : null;
            if (seller != null) {
                vo.setSellerName(seller.getNickname());
                vo.setSellerAvatar(seller.getAvatarUrl());
            }
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public void updateGoodsStatus(Long goodsId, Integer status) {
        Goods goods = this.getById(goodsId);
        if (goods == null) {
            throw new BusinessException(404, "商品不存在");
        }
        goods.setStatus(status);
        goods.setUpdateTime(new Date());
        this.updateById(goods);
    }
}
