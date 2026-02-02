package com.jin.xianqu_admin.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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

    private Integer viewCount;

    @TableField("original_price")
    private BigDecimal originalPrice;

    @TableField("category")
    private String category;

    @TableField("goods_condition")
    private String goodsCondition;

    @TableField("trading_method")
    private String tradingMethod;

    @TableField("location")
    private String location;

    @TableField("bargain_allowed")
    private Integer bargainAllowed;

    @TableField("want_count")
    private Integer wantCount;

    @TableLogic
    private Integer isDelete;

    private Date createTime;

    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
