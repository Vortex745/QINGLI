package com.jin.xianqu_admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jin.xianqu_admin.exception.BusinessException;
import com.jin.xianqu_admin.mapper.*;
import com.jin.xianqu_admin.model.dto.PageRequestDTO;
import com.jin.xianqu_admin.model.entity.*;
import com.jin.xianqu_admin.model.vo.ReportVO;
import com.jin.xianqu_admin.service.ReportService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private PartTimeJobMapper partTimeJobMapper;
    @Autowired
    private LostFoundMapper lostFoundMapper;

    @Override
    public Page<ReportVO> getList(PageRequestDTO queryDTO) {
        // 1. Query Report Page
        Page<Report> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();

        if (queryDTO.getStatus() != null) {
            queryWrapper.eq("status", queryDTO.getStatus());
        }

        // 增加对 targetType 的筛选支持 (如果 DTO 里有这个字段的话，目前 DTO 里没有，先留坑)
        // if (queryDTO.getTargetType() != null) queryWrapper.eq("target_type",
        // queryDTO.getTargetType());

        queryWrapper.orderByDesc("create_time");

        Page<Report> reportPage = this.page(page, queryWrapper);

        // 2. Convert to VO
        Page<ReportVO> voPage = new Page<>(reportPage.getCurrent(), reportPage.getSize(), reportPage.getTotal());

        List<Report> records = reportPage.getRecords();
        if (records.isEmpty()) {
            return voPage;
        }

        // 3. Collect Reporter IDs and Batch Query
        Set<Long> reporterIds = records.stream().map(Report::getReporterId).collect(Collectors.toSet());
        List<User> reporters = reporterIds.isEmpty() ? List.of() : userMapper.selectBatchIds(reporterIds);
        Map<Long, User> reporterMap = reporters.stream().collect(Collectors.toMap(User::getId, u -> u));

        // 4. Transform and Fill Data
        List<ReportVO> voList = records.stream().map(report -> {
            ReportVO vo = new ReportVO();
            BeanUtils.copyProperties(report, vo);

            // Fill Reporter Info
            User reporter = reporterMap.get(report.getReporterId());
            if (reporter != null) {
                vo.setReporterName(reporter.getNickname());
                vo.setReporterAvatar(reporter.getAvatarUrl());
            } else {
                vo.setReporterName("未知用户");
            }

            // Fill Report Type Name
            vo.setReportTypeName(getReportTypeName(report.getReportType()));

            // Fill Target Info
            fillTargetInfo(vo);

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public void handleReport(Long id, Integer status, String remark) {
        Report report = this.getById(id);
        if (report == null) {
            throw new BusinessException(404, "举报记录不存在");
        }
        report.setStatus(status);
        report.setHandleResult(remark);
        report.setHandleTime(new Date());
        // TODO: Get current admin user ID
        // report.setHandlerId(currentAdminId);

        this.updateById(report);
    }

    private void fillTargetInfo(ReportVO vo) {
        Integer type = vo.getTargetType();
        Long targetId = vo.getTargetId();

        if (type == null || targetId == null)
            return;

        String title = "未知对象";
        try {
            switch (type) {
                case 1: // Goods
                    Goods goods = goodsMapper.selectById(targetId);
                    if (goods != null)
                        title = "商品: " + goods.getName();
                    break;
                case 2: // Post
                    Post post = postMapper.selectById(targetId);
                    if (post != null) {
                        String content = post.getContent();
                        if (content != null && content.length() > 20) {
                            content = content.substring(0, 20) + "...";
                        }
                        title = "动态: " + content;
                    }
                    break;
                case 3: // PartTime
                    PartTimeJob job = partTimeJobMapper.selectById(targetId);
                    if (job != null)
                        title = "兼职: " + job.getTitle();
                    break;
                case 4: // LostFound
                    LostFound lf = lostFoundMapper.selectById(targetId);
                    if (lf != null)
                        title = "失物: " + lf.getItemName();
                    break;
                case 5: // User
                    User user = userMapper.selectById(targetId);
                    if (user != null)
                        title = "用户: " + user.getNickname();
                    break;
            }
        } catch (Exception e) {
            title = "数据加载失败";
        }
        vo.setTargetTitle(title);
    }

    private String getReportTypeName(Integer type) {
        if (type == null)
            return "未知";
        switch (type) {
            case 1:
                return "虚假信息";
            case 2:
                return "诈骗";
            case 3:
                return "违禁品";
            case 4:
                return "色情低俗";
            case 5:
                return "骚扰辱骂";
            case 6:
                return "其他";
            default:
                return "其他";
        }
    }
}
