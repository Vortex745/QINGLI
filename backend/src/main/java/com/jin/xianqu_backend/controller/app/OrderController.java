package com.jin.xianqu_backend.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jin.xianqu_backend.common.Result;
import com.jin.xianqu_backend.model.dto.OrderAddDTO;
import com.jin.xianqu_backend.model.vo.OrderVO;
import com.jin.xianqu_backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单接口
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/add")
    public Result<Long> createOrder(@RequestBody OrderAddDTO orderAddDTO) {
        Long orderId = orderService.createOrder(orderAddDTO);
        return Result.success(orderId);
    }

    @GetMapping("/list")
    public Result<Page<OrderVO>> listMyOrders(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<OrderVO> orderPage = orderService.listMyOrders(page, size);
        return Result.success(orderPage);
    }

    @GetMapping("/sold")
    public Result<Page<OrderVO>> listMySoldOrders(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<OrderVO> orderPage = orderService.listMySoldOrders(page, size);
        return Result.success(orderPage);
    }

    @GetMapping("/bought")
    public Result<Page<OrderVO>> listMyBoughtOrders(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<OrderVO> orderPage = orderService.listMyBoughtOrders(page, size);
        return Result.success(orderPage);
    }

    @PutMapping("/finish/{id}")
    public Result<Boolean> finishOrder(@PathVariable Long id) {
        boolean result = orderService.finishOrder(id);
        return Result.success(result);
    }

    @PutMapping("/cancel/{id}")
    public Result<Boolean> cancelOrder(@PathVariable Long id) {
        boolean result = orderService.cancelOrder(id);
        return Result.success(result);
    }
}
