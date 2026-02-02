package com.jin.xianqu_backend.model.vo;

import com.jin.xianqu_backend.model.entity.PartTimeJob;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PartTimeJobVO extends PartTimeJob {
    private String userNickname;
    private String userAvatar;

    // Social interactions
    private Integer favoriteCount;
    private Integer commentCount;
    private Boolean favorite;
    private Boolean bookmarked;
    private Boolean isFollowed; // 是否已关注发布者
    private Integer wantCount;
}
