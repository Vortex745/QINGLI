package com.jin.xianqu_admin.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class GoodsVO {
    private Long id;
    private Long userId;
    private Long areaId;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Integer status;
    private Integer viewCount;
    private BigDecimal originalPrice;
    private String category;
    private String goodsCondition;
    private String tradingMethod;
    private String location;
    private Integer bargainAllowed;
    private Integer wantCount;
    private Date createTime;
    private Date updateTime;

    // 发布者信息
    private String sellerName;
    private String sellerAvatar;
}
