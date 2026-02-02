package com.jin.xianqu_admin.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 帖子实体
 */
@TableName("bus_post")
@Data
public class Post implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long areaId;

    /**
     * 1-失物招领，3-校园广场
     */
    private Integer type;

    private String content;

    private String imageUrls;

    private String title;
    private String category;
    private String tags;

    /**
     * 可见范围: 0-全校, 1-本院, 2-仅好友
     */
    private Integer scope;

    /**
     * 是否匿名: 0-否, 1-是
     */
    private Integer isAnonymous;

    /**
     * 校区/地点 (用于数据隔离)
     */
    private String location;

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
