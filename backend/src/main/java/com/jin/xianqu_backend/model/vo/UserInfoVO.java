package com.jin.xianqu_backend.model.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息VO（用于刷新当前用户数据）
 */
@Data
@Builder
public class UserInfoVO implements Serializable {
    private Long id;
    private Long userId; // 兼容前端 userInfo.userId
    private String openid;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private String bgImage;
    private String mobile;
    private String role;
    private Integer status;
}
