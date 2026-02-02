package com.jin.xianqu_backend.model.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
public class GoodsVO implements Serializable {
    private Long id;
    private Long userId;
    private Long areaId;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Integer status;
    private Date createTime;

    // 扩展字段
    private String sellerName;
    private String sellerAvatar;
    private Boolean favorite; // 是否已收藏
    private Integer viewCount; // 浏览量
    private Integer favoriteCount; // 收藏量
    private Boolean isFollowed; // 当前用户是否已关注该卖家

    // 发布表单字段
    private BigDecimal originalPrice; // 原价
    private String category; // 分类
    private String condition; // 成色
    private String tradingMethod; // 交易方式
    private String location; // 交易地点
    private Integer bargainAllowed; // 是否接受议价 1-是 0-否
    private Integer wantCount; // "我想要"点击次数
}
