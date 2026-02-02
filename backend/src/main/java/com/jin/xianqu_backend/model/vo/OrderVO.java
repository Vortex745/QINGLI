package com.jin.xianqu_backend.model.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
public class OrderVO implements Serializable {
    private Long id;
    private Long goodsId;
    private String goodsName;
    private String goodsImage;
    private BigDecimal amount;
    private Integer status;
    private Date createTime;
}
