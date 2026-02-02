package com.jin.xianqu_admin.model.vo;

import lombok.Data;
import java.util.Date;

/**
 * 帖子列表返回 VO
 */
@Data
public class PostVO {
    private Long id;
    private String content;
    private String imageUrls;
    private String title;
    private String category;
    private String location;
    private Integer type;

    // 发布者信息
    private Long userId;
    private String nickname;
    private String avatarUrl;

    // 所属区域
    private String area;

    private Date createTime;

    // 是否有图片
    private Boolean hasImage;
}
