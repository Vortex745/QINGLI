package com.jin.xianqu_backend.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

/**
 * 举报请求DTO
 */
@Data
public class ReportAddDTO {

    /**
     * 被举报对象类型: 1-商品, 2-动态, 3-兼职, 4-失物招领, 5-用户
     */
    @NotNull(message = "举报对象类型不能为空")
    private Integer targetType;

    /**
     * 被举报对象ID
     */
    @NotNull(message = "举报对象ID不能为空")
    private Long targetId;

    /**
     * 被举报用户ID
     */
    private Long targetUserId;

    /**
     * 举报类型: 1-虚假信息, 2-诈骗, 3-违禁品, 4-色情低俗, 5-骚扰辱骂, 6-其他
     */
    @NotNull(message = "举报类型不能为空")
    private Integer reportType;

    /**
     * 举报理由详情
     */
    @NotBlank(message = "举报理由不能为空")
    private String reason;
}
