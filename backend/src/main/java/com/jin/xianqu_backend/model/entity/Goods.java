package com.jin.xianqu_backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品实体
 */
@TableName("bus_goods")
@Data
public class Goods implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long areaId;

    private String name;

    private String description;

    private BigDecimal price;

    private String imageUrl;

    /**
     * 0-上架，1-交易中，2-已售出，3-下架
     */
    private Integer status;

    /**
     * 浏览量
     */
    private Integer viewCount;

    // 发布表单详细信息
    @TableField("original_price")
    private BigDecimal originalPrice; // 原价
    @TableField("category")
    private String category; // 分类
    @TableField("goods_condition")
    private String goodsCondition; // 成色：全新、几乎全新等
    @TableField("trading_method")
    private String tradingMethod; // 交易方式：当面交易、邮寄、都可以
    @TableField("location")
    private String location; // 交易地点
    @TableField("bargain_allowed")
    private Integer bargainAllowed; // 是否接受议价 1-是 0-否
    @TableField("want_count")
    private Integer wantCount; // "我想要"点击次数

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer isDelete;

    private Date createTime;

    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
