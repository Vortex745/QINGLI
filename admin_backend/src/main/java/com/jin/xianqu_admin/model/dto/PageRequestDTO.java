package com.jin.xianqu_admin.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class PageRequestDTO implements Serializable {
    private Integer page = 1;
    private Integer size = 10;
    private String keyword;
    private String category;
    private Integer status;
    private Long userId; // Optional for filteringByUser
}
