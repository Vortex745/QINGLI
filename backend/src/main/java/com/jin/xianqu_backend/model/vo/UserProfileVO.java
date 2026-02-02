package com.jin.xianqu_backend.model.vo;

import lombok.Data;
import com.jin.xianqu_backend.model.entity.User;

@Data
public class UserProfileVO {
    private User userInfo;
    private Integer followerCount;
    private Integer followingCount;
    private Boolean isFollowed; // Current user is following target user
    private Integer goodsCount;
}
