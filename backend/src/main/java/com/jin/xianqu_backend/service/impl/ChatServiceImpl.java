package com.jin.xianqu_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jin.xianqu_backend.mapper.ChatMessageMapper;
import com.jin.xianqu_backend.mapper.UserMapper;
import com.jin.xianqu_backend.model.entity.ChatMessage;
import com.jin.xianqu_backend.model.entity.User;
import com.jin.xianqu_backend.model.vo.ChatSessionVO;
import com.jin.xianqu_backend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public ChatMessage saveMessage(Long senderId, Long receiverId, String content, Integer type, Long goodsId) {
        ChatMessage msg = new ChatMessage();
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setType(type != null ? type : 0);
        msg.setGoodsId(goodsId);
        msg.setIsRead(0);
        msg.setCreateTime(new Date());
        this.save(msg);
        return msg;
    }

    @Override
    public List<ChatSessionVO> getSessionList(Long userId) {
        List<ChatMessage> recentMsgs = chatMessageMapper.selectRecentSessions(userId);

        return recentMsgs.stream().map(msg -> {
            Long otherUserId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
            User otherUser = userMapper.selectById(otherUserId);

            // Calculate unread from this user
            // If I am the receiver, count unread messages from otherUser
            Integer unread = 0;
            if (msg.getReceiverId().equals(userId)) {
                QueryWrapper<ChatMessage> countQuery = new QueryWrapper<>();
                countQuery.eq("sender_id", otherUserId);
                countQuery.eq("receiver_id", userId);
                countQuery.eq("is_read", 0);
                unread = Math.toIntExact(this.count(countQuery));
            }

            return ChatSessionVO.builder()
                    .otherUserId(otherUserId)
                    .otherUserName(otherUser != null ? otherUser.getNickname() : "Unknown")
                    .otherUserAvatar(otherUser != null ? otherUser.getAvatarUrl() : "")
                    .lastContent(msg.getType() == 1 ? "[图片]" : (msg.getType() == 2 ? "[商品]" : msg.getContent()))
                    .lastTime(msg.getCreateTime())
                    .unreadCount(unread)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<ChatMessage> getChatHistory(Long userId, Long otherUserId, Long beforeId, int limit) {
        QueryWrapper<ChatMessage> query = new QueryWrapper<>();
        query.and(wrapper -> wrapper
                .eq("sender_id", userId).eq("receiver_id", otherUserId)
                .or()
                .eq("sender_id", otherUserId).eq("receiver_id", userId));
        if (beforeId != null) {
            query.lt("id", beforeId);
        }
        query.orderByDesc("create_time");
        query.last("LIMIT " + limit);

        // Return sorted by time ASC for frontend convenience if needed, but usually
        // desc is easier for paging
        return this.list(query);
    }

    @Override
    public void markAsRead(Long userId, Long otherUserId) {
        chatMessageMapper.markAsRead(otherUserId, userId); // Sender is otherUser, Receiver is me
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        return chatMessageMapper.countUnread(userId);
    }

    @Override
    public List<ChatMessage> getSystemNoticeList(Long userId) {
        System.out.println("DEBUG: Querying system notices for userId: " + userId);
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getType, 9)
                .orderByDesc(ChatMessage::getCreateTime);

        if (userId != null) {
            wrapper.and(w -> w.eq(ChatMessage::getReceiverId, userId).or().eq(ChatMessage::getReceiverId, 0L));
        }

        List<ChatMessage> list = this.list(wrapper);
        System.out.println("DEBUG: Found " + list.size() + " notices");
        return list;
    }

    @Override
    public void markMessageAsRead(Long messageId) {
        this.update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ChatMessage>()
                .eq(ChatMessage::getId, messageId)
                .set(ChatMessage::getIsRead, 1));
    }
}
