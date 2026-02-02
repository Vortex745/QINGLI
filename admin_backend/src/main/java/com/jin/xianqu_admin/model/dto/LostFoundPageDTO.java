package com.jin.xianqu_admin.model.dto;

import lombok.Data;

@Data
public class LostFoundPageDTO {
    private Integer page = 1;
    private Integer size = 10;
    private Integer type; // 0-失物, 1-招领
    private String itemName; // 物品名称
    private Integer status; // 状态筛选
}
