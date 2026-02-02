package com.jin.xianqu_backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 举报实体
 */
@TableName("bus_report")
@Data
public class Report implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 举报人ID
     */
    private Long reporterId;

    /**
     * 被举报对象类型: 1-商品, 2-动态, 3-兼职, 4-失物招领, 5-用户
     */
    private Integer targetType;

    /**
     * 被举报对象ID
     */
    private Long targetId;

    /**
     * 被举报用户ID (商品/动态等的发布者)
     */
    private Long targetUserId;

    /**
     * 举报类型: 1-虚假信息, 2-诈骗, 3-违禁品, 4-色情低俗, 5-骚扰辱骂, 6-其他
     */
    private Integer reportType;

    /**
     * 举报理由详情
     */
    private String reason;

    /**
     * 处理状态: 0-待处理, 1-已处理, 2-已驳回
     */
    private Integer status;

    /**
     * 处理结果说明
     */
    private String handleResult;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理时间
     */
    private Date handleTime;

    @TableLogic
    private Integer isDelete;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
