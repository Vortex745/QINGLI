package com.jin.xianqu_admin.model.dto;

import lombok.Data;

@Data
public class PostPageDTO {
    private Integer page = 1;
    private Integer size = 10;
    private String content; // 内容关键字
    private String nickname; // 发布者昵称
    private String area; // 所属学校/区域
    private Integer type; // 类型: 3-校园广场
}
