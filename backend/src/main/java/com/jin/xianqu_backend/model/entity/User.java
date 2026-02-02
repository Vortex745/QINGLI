package com.jin.xianqu_backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户实体
 */
@TableName("sys_user")
@Data
public class User implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String openid;

    private String nickname;

    private String avatarUrl;

    /**
     * 用户名 (后台管理员登录用)
     */
    private String username;

    /**
     * 密码 (后台管理员登录用)
     */
    private String password;

    private String role;

    /**
     * 当前所在区域ID
     */
    private Long currentAreaId;

    private Date createTime;

    private String bio;

    private String bgImage;

    private String mobile;

    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
