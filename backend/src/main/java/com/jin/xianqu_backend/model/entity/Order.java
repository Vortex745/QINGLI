package com.jin.xianqu_backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单实体
 */
@TableName("bus_order")
@Data
public class Order implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long buyerId;

    private Long sellerId;

    private Long goodsId;

    private BigDecimal amount;

    /**
     * 0-进行中, 1-交易成功, 2-已取消
     */
    private Integer status;

    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
