package com.jin.xianqu_admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("sys_user")
public class User implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String openid;
    private String nickname;
    private String phone;
    private String avatarUrl;
    private String username;
    private String password;
    private String role;
    private Integer status; // 1: Active, 0: Banned
    private java.util.Date createTime;
    private java.util.Date updateTime;
}
