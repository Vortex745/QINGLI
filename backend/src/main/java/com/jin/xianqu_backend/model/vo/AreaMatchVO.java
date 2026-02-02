package com.jin.xianqu_backend.model.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 区域匹配返回对象
 */
@Data
@Builder
public class AreaMatchVO implements Serializable {
    private Long id;
    private String name;
    private String type;
    private Double distance;
}
