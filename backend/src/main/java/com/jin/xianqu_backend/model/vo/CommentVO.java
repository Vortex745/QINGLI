package com.jin.xianqu_backend.model.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
public class CommentVO implements Serializable {
    private Long id;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private String content;
    private Date createTime;
}
