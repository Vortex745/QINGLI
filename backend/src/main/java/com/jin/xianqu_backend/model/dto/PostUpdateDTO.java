package com.jin.xianqu_backend.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PostUpdateDTO implements Serializable {
    private Long id;
    private String title;
    private String content;
    private String category;
    private String tags;
    private String location;
    private Integer isAnonymous;
    private String imageUrls;
    private Integer status;
}
