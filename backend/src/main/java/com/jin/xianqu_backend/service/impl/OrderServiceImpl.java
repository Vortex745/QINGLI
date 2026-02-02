package com.jin.xianqu_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jin.xianqu_backend.common.UserContext;
import com.jin.xianqu_backend.exception.BusinessException;
import com.jin.xianqu_backend.mapper.GoodsMapper;
import com.jin.xianqu_backend.mapper.OrderMapper;
import com.jin.xianqu_backend.model.dto.OrderAddDTO;
import com.jin.xianqu_backend.model.entity.Goods;
import com.jin.xianqu_backend.model.entity.Order;
import com.jin.xianqu_backend.model.vo.OrderVO;
import com.jin.xianqu_backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderAddDTO orderAddDTO) {
        Long buyerId = UserContext.getUserId();
        if (buyerId == null) {
            throw new BusinessException(401, "未登录");
        }

        Long goodsId = orderAddDTO.getGoodsId();
        // 1. 校验商品
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (goods.getStatus() != 0) {
            throw new BusinessException(400, "商品已下架或正在交易中");
        }
        if (goods.getUserId().equals(buyerId)) {
            throw new BusinessException(400, "不能购买自己的商品");
        }

        // 2. 更新商品状态为交易中 (1)
        goods.setStatus(1);
        goodsMapper.updateById(goods);

        // 3. 创建订单
        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setSellerId(goods.getUserId());
        order.setGoodsId(goodsId);
        order.setAmount(goods.getPrice());
        order.setStatus(0); // 进行中
        order.setCreateTime(new Date());

        this.save(order);

        return order.getId();
    }

    @Override
    public Page<OrderVO> listMyOrders(int page, int size) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        Page<Order> pageParam = new Page<>(page, size);
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        // 查询我是买家或者卖家的订单
        queryWrapper.eq("buyer_id", userId).or().eq("seller_id", userId);
        queryWrapper.orderByDesc("create_time");

        Page<Order> orderPage = this.page(pageParam, queryWrapper);

        // 组装 VO
        List<OrderVO> voList = orderPage.getRecords().stream().map(order -> {
            Goods goods = goodsMapper.selectById(order.getGoodsId());
            return OrderVO.builder()
                    .id(order.getId())
                    .goodsId(order.getGoodsId())
                    .goodsName(goods != null ? goods.getName() : "未知商品")
                    .goodsImage(goods != null ? goods.getImageUrl() : "")
                    .amount(order.getAmount())
                    .status(order.getStatus())
                    .createTime(order.getCreateTime())
                    .build();
        }).collect(Collectors.toList());

        Page<OrderVO> resultPage = new Page<>(page, size);
        resultPage.setRecords(voList);
        resultPage.setTotal(orderPage.getTotal());

        return resultPage;
    }

    @Override
    public Page<OrderVO> listMySoldOrders(int page, int size) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        Page<Order> pageParam = new Page<>(page, size);
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("seller_id", userId);
        queryWrapper.orderByDesc("create_time");

        Page<Order> orderPage = this.page(pageParam, queryWrapper);
        return convertOrderToVO(orderPage, page, size);
    }

    @Override
    public Page<OrderVO> listMyBoughtOrders(int page, int size) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        Page<Order> pageParam = new Page<>(page, size);
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("buyer_id", userId);
        queryWrapper.orderByDesc("create_time");

        Page<Order> orderPage = this.page(pageParam, queryWrapper);
        return convertOrderToVO(orderPage, page, size);
    }

    private Page<OrderVO> convertOrderToVO(Page<Order> orderPage, int page, int size) {
        List<OrderVO> voList = orderPage.getRecords().stream().map(order -> {
            Goods goods = goodsMapper.selectById(order.getGoodsId());
            return OrderVO.builder()
                    .id(order.getId())
                    .goodsId(order.getGoodsId())
                    .goodsName(goods != null ? goods.getName() : "未知商品")
                    .goodsImage(goods != null ? goods.getImageUrl() : "")
                    .amount(order.getAmount())
                    .status(order.getStatus())
                    .createTime(order.getCreateTime())
                    .build();
        }).collect(Collectors.toList());

        Page<OrderVO> resultPage = new Page<>(page, size);
        resultPage.setRecords(voList);
        resultPage.setTotal(orderPage.getTotal());

        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean finishOrder(Long id) {
        Long userId = UserContext.getUserId();
        Order order = this.getById(id);
        if (order == null)
            throw new BusinessException(404, "订单不存在");

        // 只有买家或卖家可以完成？通常买家确认收货，或者卖家可以点击完成（面对面）。这里简单处理：买家确认
        // 或者允许任一方点击“已完成交易”
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }

        if (order.getStatus() != 0) {
            throw new BusinessException(400, "订单状态不正确");
        }

        order.setStatus(1); // 交易成功

        // 商品状态维持在 1 (交易中) 或者改成 2 (已售出)，这里假设 1 就代表不可买。甚至可以删掉商品？
        // 这里我们把商品状态改为 2 (已售出/下架)，如果业务需要
        // 之前逻辑：0上架，1交易中。
        // 现在增加：2已售出
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        if (goods != null) {
            goods.setStatus(2); // 已售出，不再显示或显示已售
            goodsMapper.updateById(goods);
        }

        return this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long id) {
        Long userId = UserContext.getUserId();
        Order order = this.getById(id);
        if (order == null)
            throw new BusinessException(404, "订单不存在");

        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }

        if (order.getStatus() != 0) {
            throw new BusinessException(400, "订单状态不正确");
        }

        order.setStatus(2); // 已取消

        // 核心：释放商品（状态回滚到 0）
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        if (goods != null) {
            goods.setStatus(0);
            goodsMapper.updateById(goods);
        }

        return this.updateById(order);
    }
}
