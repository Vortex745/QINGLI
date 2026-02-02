package com.jin.xianqu_backend.model.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class LoginVO implements Serializable {
    private Long id;
    private String openid;
    private String token;
    private String nickname;
    private String avatarUrl;
    private String mobile;
}
