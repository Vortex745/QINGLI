package com.jin.xianqu_backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

/**
 * 失物招领实体
 */
@TableName("bus_lost_found")
@Data
public class LostFound implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 0-失物, 1-招领
     */
    private Integer type;

    private String itemName;

    private String category;

    private Date time;

    private String location;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String features;

    private String imageUrls;

    private String contactInfo;

    /**
     * 0-否 1-是
     */
    private Integer isPublic;

    /**
     * 0-发布中 1-已找回/已归还 2-已下架
     */
    private Integer status;

    @TableLogic
    private Integer isDelete;

    private Integer viewCount;
    private Integer wantCount;

    private Date createTime;

    private Date updateTime;

    /**
     * 所属校区
     */
    private String university;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
