package com.jin.xianqu_backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论实体
 */
@TableName("bus_comment")
@Data
public class Comment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long targetId;

    /**
     * 1-商品，2-帖子
     */
    private Integer type;

    private String content;

    @TableLogic
    private Integer isDelete;

    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
