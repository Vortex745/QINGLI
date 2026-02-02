package com.jin.xianqu_admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jin.xianqu_admin.mapper.ChatMessageMapper;
import com.jin.xianqu_admin.mapper.NoticeMapper;
import com.jin.xianqu_admin.mapper.UserMapper;
import com.jin.xianqu_admin.model.dto.PageRequestDTO;
import com.jin.xianqu_admin.model.entity.ChatMessage;
import com.jin.xianqu_admin.model.entity.Notice;
import com.jin.xianqu_admin.model.entity.User;
import com.jin.xianqu_admin.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Page<Notice> getList(PageRequestDTO queryDTO) {
        Page<Notice> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        QueryWrapper<Notice> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return this.page(page, queryWrapper);
    }

    @Override
    public void sendNotice(Notice notice) {
        try {
            Date now = new Date();
            if (notice.getCreateTime() == null) {
                notice.setCreateTime(now);
            }
            // 保存到 sys_notice 表（管理后台记录）
            this.save(notice);

            // 同时插入到 bus_chat_message 表，让前端小程序能读取系统通知
            // type=9 代表系统通知, senderId=0 代表系统
            String messageContent = notice.getTitle() + "\n" + notice.getContent();

            if (notice.getType() == 0) {
                // 全员通知：插入一条 receiverId=0 的全员消息（读扩散模式）
                insertSystemMessage(0L, messageContent, now);
            } else if (notice.getType() == 1 && notice.getTargetId() != null) {
                // 指定用户通知
                insertSystemMessage(notice.getTargetId(), messageContent, now);
            }
        } catch (Exception e) {
            try {
                java.nio.file.Files.write(java.nio.file.Paths.get("send_notice_error.txt"),
                        (e.toString() + "\n").getBytes());
                e.printStackTrace();
            } catch (Exception ex) {
            }
            throw new RuntimeException("发送通知失败: " + e.getMessage());
        }
    }

    private void insertSystemMessage(Long receiverId, String content, Date createTime) {
        try {
            ChatMessage msg = new ChatMessage();
            msg.setSenderId(0L); // 0 代表系统
            msg.setReceiverId(receiverId); // 0 代表所有用户
            msg.setContent(content);
            msg.setType(9); // 9 = 系统通知
            msg.setIsRead(0);
            msg.setCreateTime(createTime);
            chatMessageMapper.insert(msg);
        } catch (Exception e) {
            try {
                java.io.StringWriter sw = new java.io.StringWriter();
                java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                e.printStackTrace(pw);
                java.nio.file.Files.write(java.nio.file.Paths.get("error_trace.txt"), sw.toString().getBytes());
            } catch (Exception ex) {
            }
            e.printStackTrace();
            throw new RuntimeException("插入系统消息失败: " + e.getMessage());
        }
    }
}
