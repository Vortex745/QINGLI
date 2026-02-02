package com.jin.xianqu_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jin.xianqu_backend.model.entity.ChatMessage;
import com.jin.xianqu_backend.model.vo.ChatSessionVO;

import java.util.List;

public interface ChatService extends IService<ChatMessage> {

    // 保存消息
    ChatMessage saveMessage(Long senderId, Long receiverId, String content, Integer type, Long goodsId);

    // 获取会话列表
    List<ChatSessionVO> getSessionList(Long userId);

    // 获取特定会话的历史消息
    List<ChatMessage> getChatHistory(Long userId, Long otherUserId, Long beforeId, int limit);

    // 标记已读
    void markAsRead(Long userId, Long otherUserId);

    // 获取未读总数
    Integer getUnreadCount(Long userId);

    // 获取系统通知
    java.util.List<com.jin.xianqu_backend.model.entity.ChatMessage> getSystemNoticeList(Long userId);

    // 标记单条消息已读
    void markMessageAsRead(Long messageId);
}
