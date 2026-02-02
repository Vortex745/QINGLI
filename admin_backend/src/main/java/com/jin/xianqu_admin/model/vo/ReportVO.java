package com.jin.xianqu_admin.model.vo;

import com.jin.xianqu_admin.model.entity.Report; // Assuming Report is in entity package
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReportVO extends Report {

    private String reporterName;
    private String reporterAvatar;

    private String targetTitle; // 商品名、帖子标题、用户名等
    private String targetImage; // 封面图

    // 中文翻译字段，方便前端（可选，前端也可以自己转）
    private String reportTypeName;
    private String targetTypeName;
}
