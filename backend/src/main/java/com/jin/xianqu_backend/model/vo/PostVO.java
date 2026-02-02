package com.jin.xianqu_backend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "帖子展示对象")
public class PostVO implements Serializable {
    @Schema(description = "帖子ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "发布者ID")
    private Long userId;

    @Schema(description = "区域ID")
    private Long areaId;

    @Schema(description = "帖子类型(1-失物招领, 3-校园广场)")
    private Integer type;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "图片URL列表(逗号分隔)")
    private String imageUrls;

    @Schema(description = "发布时间")
    private Date createTime;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "可见范围")
    private Integer scope;

    @Schema(description = "是否匿名")
    private Integer isAnonymous;

    @Schema(description = "发布者昵称")
    private String userNickname;

    @Schema(description = "发布者头像")
    private String userAvatar;

    @Schema(description = "当前用户是否点赞")
    private Boolean favorite;

    @Schema(description = "当前用户是否收藏")
    private Boolean bookmarked;

    @Schema(description = "点赞数量")
    private Integer favoriteCount;

    @Schema(description = "评论数量")
    private Integer commentCount;

    @Schema(description = "校区/地点")
    private String location;

    @Schema(description = "状态: 0-正常, 1-已下架")
    private Integer status;

    private Boolean isFollowed; // 是否已关注发布者

    private Integer viewCount;
    private Integer wantCount;

    private static final long serialVersionUID = 1L;
}
