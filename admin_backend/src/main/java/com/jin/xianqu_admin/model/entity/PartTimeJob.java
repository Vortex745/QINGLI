package com.jin.xianqu_admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("part_time_job")
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
    private Integer status; // 0-招聘中, 1-已招满, 2-已下架
    private Date createTime;
    private Date updateTime;
    private String location;
}
