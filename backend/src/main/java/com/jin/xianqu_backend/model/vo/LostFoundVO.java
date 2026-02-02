package com.jin.xianqu_backend.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;
import java.util.List;

@Data
public class LostFoundVO implements Serializable {
    private Long id;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private Integer type; // 0-Lost, 1-Found
    private String itemName;
    private String category;
    private Date time;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String features;
    private List<String> imageUrls;
    private String contactInfo;
    private Integer isPublic;
    private Integer status;
    private Date createTime;
    private String university;

    // Social
    private Integer favoriteCount;
    private Boolean favorite;
    private Boolean bookmarked;
    private Integer commentCount;
    private Boolean isFollowed; // 是否已关注发布者
    private Integer viewCount;
    private Integer wantCount;
}
