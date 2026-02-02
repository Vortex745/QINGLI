package com.jin.xianqu_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jin.xianqu_backend.common.UserContext;
import com.jin.xianqu_backend.exception.BusinessException;
import com.jin.xianqu_backend.mapper.ChatMessageMapper;
import com.jin.xianqu_backend.mapper.ReportMapper;
import com.jin.xianqu_backend.mapper.UserMapper;
import com.jin.xianqu_backend.mapper.GoodsMapper;
import com.jin.xianqu_backend.model.dto.ReportAddDTO;
import com.jin.xianqu_backend.model.entity.ChatMessage;
import com.jin.xianqu_backend.model.entity.Report;
import com.jin.xianqu_backend.model.entity.User;
import com.jin.xianqu_backend.model.entity.Goods;
import com.jin.xianqu_backend.model.vo.ReportVO;
import com.jin.xianqu_backend.service.ReportService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    private static final Map<Integer, String> TARGET_TYPE_MAP = new HashMap<>();
    private static final Map<Integer, String> REPORT_TYPE_MAP = new HashMap<>();
    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();

    static {
        TARGET_TYPE_MAP.put(1, "商品");
        TARGET_TYPE_MAP.put(2, "动态");
        TARGET_TYPE_MAP.put(3, "兼职");
        TARGET_TYPE_MAP.put(4, "失物招领");
        TARGET_TYPE_MAP.put(5, "用户");

        REPORT_TYPE_MAP.put(1, "虚假信息");
        REPORT_TYPE_MAP.put(2, "诈骗");
        REPORT_TYPE_MAP.put(3, "违禁品");
        REPORT_TYPE_MAP.put(4, "色情低俗");
        REPORT_TYPE_MAP.put(5, "骚扰辱骂");
        REPORT_TYPE_MAP.put(6, "其他");

        STATUS_MAP.put(0, "待处理");
        STATUS_MAP.put(1, "已处理");
        STATUS_MAP.put(2, "已驳回");
    }

    @Override
    public Boolean submitReport(ReportAddDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        // Check duplicate report
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getReporterId, userId)
                .eq(Report::getTargetType, dto.getTargetType())
                .eq(Report::getTargetId, dto.getTargetId())
                .eq(Report::getStatus, 0);
        if (this.count(wrapper) > 0) {
            throw new BusinessException(400, "您已提交过举报，请耐心等待处理");
        }

        // Create report
        Report report = new Report();
        BeanUtils.copyProperties(dto, report);
        report.setReporterId(userId);
        report.setStatus(0);
        report.setCreateTime(new Date());

        boolean saved = this.save(report);

        if (saved) {
            // Send system comfort message to reporter
            sendComfortMessage(userId);
        }

        return saved;
    }

    /**
     * 发送安慰信给举报人
     */
    private void sendComfortMessage(Long userId) {
        ChatMessage message = new ChatMessage();
        message.setSenderId(0L); // 0 represents system
        message.setReceiverId(userId);
        message.setContent(
                "【系统通知】感谢您的举报反馈！我们已收到您的举报信息，平台将在1-3个工作日内进行核实处理。如情况属实，我们将对违规内容或用户进行相应处理。感谢您对平台环境的维护，您的每一次反馈都是我们进步的动力！");
        message.setType(9); // System notification
        message.setIsRead(0);
        message.setCreateTime(new Date());
        chatMessageMapper.insert(message);
    }

    @Override
    public Page<ReportVO> listReports(int page, int size, Integer status, Integer targetType) {
        Page<Report> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Report::getStatus, status);
        }
        if (targetType != null) {
            wrapper.eq(Report::getTargetType, targetType);
        }

        wrapper.orderByDesc(Report::getCreateTime);

        Page<Report> reportPage = this.page(pageParam, wrapper);

        // Convert to VO
        List<ReportVO> voList = reportPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());

        Page<ReportVO> voPage = new Page<>(page, size, reportPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    private ReportVO convertToVO(Report report) {
        ReportVO vo = new ReportVO();
        if (report == null) {
            return vo;
        }
        BeanUtils.copyProperties(report, vo);

        // Set type names
        vo.setTargetTypeName(TARGET_TYPE_MAP.getOrDefault(report.getTargetType(), "未知"));
        vo.setReportTypeName(REPORT_TYPE_MAP.getOrDefault(report.getReportType(), "其他"));
        vo.setStatusName(STATUS_MAP.getOrDefault(report.getStatus(), "未知"));

        // Get reporter info
        User reporter = userMapper.selectById(report.getReporterId());
        if (reporter != null) {
            vo.setReporterName(reporter.getNickname());
            vo.setReporterAvatar(reporter.getAvatarUrl());
        }

        // Get target user info
        if (report.getTargetUserId() != null) {
            User targetUser = userMapper.selectById(report.getTargetUserId());
            if (targetUser != null) {
                vo.setTargetUserName(targetUser.getNickname());
            }
        }

        // Get target title based on type
        if (report.getTargetType() == 1 && report.getTargetId() != null) {
            Goods goods = goodsMapper.selectById(report.getTargetId());
            if (goods != null) {
                vo.setTargetTitle(goods.getName());
                if (report.getTargetUserId() == null) {
                    vo.setTargetUserId(goods.getUserId());
                    User owner = userMapper.selectById(goods.getUserId());
                    if (owner != null) {
                        vo.setTargetUserName(owner.getNickname());
                    }
                }
            }
        }

        return vo;
    }

    @Override
    public Boolean handleReport(Long id, Integer status, String handleResult) {
        Long handlerId = UserContext.getUserId();

        Report report = this.getById(id);
        if (report == null) {
            throw new BusinessException(404, "举报信息不存在");
        }

        report.setStatus(status);
        report.setHandleResult(handleResult);
        report.setHandlerId(handlerId);
        report.setHandleTime(new Date());
        report.setUpdateTime(new Date());

        return this.updateById(report);
    }
}
