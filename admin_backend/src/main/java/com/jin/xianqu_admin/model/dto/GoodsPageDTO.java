package com.jin.xianqu_admin.model.dto;

import lombok.Data;

@Data
public class GoodsPageDTO {
    private Integer page = 1;
    private Integer size = 20;
    private String name;
    private String category;
    private Integer status;
}
