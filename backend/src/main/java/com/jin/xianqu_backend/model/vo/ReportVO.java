package com.jin.xianqu_backend.model.vo;

import lombok.Data;
import java.util.Date;

/**
 * 举报信息VO (用于管理后台展示)
 */
@Data
public class ReportVO {

    private Long id;

    /**
     * 举报人ID
     */
    private Long reporterId;

    /**
     * 举报人昵称
     */
    private String reporterName;

    /**
     * 举报人头像
     */
    private String reporterAvatar;

    /**
     * 被举报对象类型: 1-商品, 2-动态, 3-兼职, 4-失物招领, 5-用户
     */
    private Integer targetType;

    /**
     * 被举报对象类型名称
     */
    private String targetTypeName;

    /**
     * 被举报对象ID
     */
    private Long targetId;

    /**
     * 被举报对象标题/名称
     */
    private String targetTitle;

    /**
     * 被举报用户ID
     */
    private Long targetUserId;

    /**
     * 被举报用户昵称
     */
    private String targetUserName;

    /**
     * 举报类型: 1-虚假信息, 2-诈骗, 3-违禁品, 4-色情低俗, 5-骚扰辱骂, 6-其他
     */
    private Integer reportType;

    /**
     * 举报类型名称
     */
    private String reportTypeName;

    /**
     * 举报理由详情
     */
    private String reason;

    /**
     * 处理状态: 0-待处理, 1-已处理, 2-已驳回
     */
    private Integer status;

    /**
     * 处理状态名称
     */
    private String statusName;

    /**
     * 处理结果说明
     */
    private String handleResult;

    /**
     * 处理时间
     */
    private Date handleTime;

    /**
     * 创建时间
     */
    private Date createTime;
}
