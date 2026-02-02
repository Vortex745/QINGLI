package com.jin.xianqu_backend.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PostAddDTO implements Serializable {
    private Long id; // Optional: for update. If null, it's a new post.
    private String content;
    private String imageUrls;
    private Integer type; // 1-失物招领 3-广场
    private String title;
    private String category;
    private String tags;
    private Integer scope; // 0-全校 1-本院 2-仅好友
    private Integer isAnonymous; // 0-否 1-是
    private String location;
    private Integer status; // 0-active/visible, 1-off-shelf/hidden
}
