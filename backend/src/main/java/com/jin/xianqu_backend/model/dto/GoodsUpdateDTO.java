package com.jin.xianqu_backend.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GoodsUpdateDTO implements Serializable {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Integer status; // 0-上架, 1-交易中, 2-已售出, 3-下架

    // 可选的详细信息字段
    private BigDecimal originalPrice; // 原价
    private String category; // 分类
    private String condition; // 成色
    private String tradingMethod; // 交易方式
    private String location; // 交易地点
    private Integer bargainAllowed; // 是否接受议价 1-是 0-否
}
