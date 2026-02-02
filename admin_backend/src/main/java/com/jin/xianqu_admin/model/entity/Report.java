package com.jin.xianqu_admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("bus_report")
public class Report implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reporterId;

    // 1-Goods, 2-Post, 3-PartTime, 4-LostFound, 5-User
    private Integer targetType;

    private Long targetId;

    private Long targetUserId; // 被举报人ID

    // 1-False Info, 2-Scam, 3-Contraband, 4-Porn, 5-Harassment, 6-Other
    private Integer reportType;

    private String reason;

    // 0-Pending, 1-Handled, 2-Rejected
    private Integer status;

    private String handleResult;

    private Long handlerId;

    private Date handleTime;

    private Integer isDelete;

    private Date createTime;

    private Date updateTime;
}
