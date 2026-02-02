package com.jin.xianqu_backend.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class FavoriteToggleDTO implements Serializable {
    private Long targetId;
    private Integer type; // 1-商品, 2-帖子
}
