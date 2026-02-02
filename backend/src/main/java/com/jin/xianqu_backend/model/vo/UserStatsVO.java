package com.jin.xianqu_backend.model.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class UserStatsVO implements Serializable {
    private Long publishedCount;
    private Long soldCount;
    private Long boughtCount;
    private Long favoriteCount;
    private Long followerCount;
    private Long followingCount;
}
