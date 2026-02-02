package com.jin.xianqu_backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 区域实体
 */
@TableName("sys_area")
@Data
public class Area implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /**
     * 类型：school-高校，community-社区
     */
    private String type;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
