package com.jin.xianqu_backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "part_time_job")
@Data
public class PartTimeJob implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    private String workTime;

    private String workLocation;

    private String salary;

    private String settlementMethod;

    private Integer recruitCount;

    private String studentRequirement;

    private String tags;

    private String contactInfo;

    private Integer status;

    private Integer viewCount;

    /**
     * 校区/地点 (用于数据隔离)
     */
    private String location;

    private Integer wantCount;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
