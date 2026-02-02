package com.jin.xianqu_backend.model.vo;

import lombok.Data;

@Data
public class UserSimpleVO {
    private Long id;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private Boolean isFollowed;
}
