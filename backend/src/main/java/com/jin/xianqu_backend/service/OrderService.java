package com.jin.xianqu_backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jin.xianqu_backend.model.dto.OrderAddDTO;
import com.jin.xianqu_backend.model.entity.Order;
import com.jin.xianqu_backend.model.vo.OrderVO;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     * 
     * @param orderAddDTO
     * @return 订单ID
     */
    Long createOrder(OrderAddDTO orderAddDTO);

    /**
     * 获取我的订单列表
     * 
     * @param page
     * @param size
     * @return
     */
    Page<OrderVO> listMyOrders(int page, int size);

    /**
     * 获取我卖出的订单列表
     */
    Page<OrderVO> listMySoldOrders(int page, int size);

    /**
     * 获取我买到的订单列表
     */
    Page<OrderVO> listMyBoughtOrders(int page, int size);

    /**
     * 完成订单
     * 
     * @param id
     * @return
     */
    boolean finishOrder(Long id);

    /**
     * 取消订单
     * 
     * @param id
     * @return
     */
    boolean cancelOrder(Long id);
}
