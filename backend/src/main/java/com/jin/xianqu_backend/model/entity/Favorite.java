package com.jin.xianqu_backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 收藏/点赞实体
 */
@TableName("bus_favorite")
@Data
public class Favorite implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long targetId;

    /**
     * 1-商品，2-帖子
     */
    private Integer type;

    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
