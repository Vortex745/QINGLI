package com.jin.xianqu_admin.service.impl;

import com.jin.xianqu_admin.mapper.ChatMessageMapper;
import com.jin.xianqu_admin.model.entity.ChatMessage;
import com.jin.xianqu_admin.service.AdminUserService;
import com.jin.xianqu_admin.service.SystemNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemNoticeServiceImpl implements SystemNoticeService {

    private final ChatMessageMapper chatMessageMapper;
    private final AdminUserService adminUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendNotice(String target, String content) {
        if ("all".equals(target)) {
            // 全员发送
            List<Long> userIds = adminUserService.getAllUserIds();
            for (Long userId : userIds) {
                saveSystemMessage(userId, content);
            }
        } else {
            // 指定用户发送
            try {
                Long userId = Long.parseLong(target);
                saveSystemMessage(userId, content);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid user ID");
            }
        }
    }

    private void saveSystemMessage(Long receiverId, String content) {
        ChatMessage message = new ChatMessage();
        message.setSenderId(0L); // 0 代表系统/管理员
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setType(9); // 系统通知
        message.setIsRead(0);
        message.setCreateTime(new Date());
        chatMessageMapper.insert(message);
    }
}
