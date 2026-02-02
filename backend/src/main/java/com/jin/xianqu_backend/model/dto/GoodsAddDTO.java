package com.jin.xianqu_backend.model.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class GoodsAddDTO {

    @NotBlank(message = "名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "价格不能为空")
    @Min(value = 0, message = "价格不能小于0")
    private BigDecimal price;

    @NotBlank(message = "图片不能为空")
    private String imageUrl;

    // 可选的详细信息字段
    private BigDecimal originalPrice; // 原价
    private String category; // 分类
    private String condition; // 成色
    private String tradingMethod; // 交易方式
    private String location; // 交易地点
    private Integer bargainAllowed; // 是否接受议价 1-是 0-否
}
