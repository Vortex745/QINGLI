package com.jin.xianqu_backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户关注实体
 */
@TableName("user_follows")
@Data
public class UserFollow implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long followerId;

    private Long followedId;

    private Date createTime;

    private static final long serialVersionUID = 1L;
}
