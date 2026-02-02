package com.jin.xianqu_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jin.xianqu_backend.model.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    // 获取当前用户的会话列表（最近一条消息）
    // 这是一个比较复杂的查询，通常需要 group by
    @Select("SELECT m.* FROM bus_chat_message m " +
            "WHERE m.id IN (" +
            "  SELECT MAX(id) FROM bus_chat_message " +
            "  WHERE (sender_id = #{userId} OR receiver_id = #{userId}) " +
            "  AND type != 9 " +
            "  GROUP BY CASE " +
            "    WHEN sender_id = #{userId} THEN receiver_id " +
            "    ELSE sender_id " +
            "  END" +
            ") " +
            "ORDER BY m.create_time DESC")
    List<ChatMessage> selectRecentSessions(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM bus_chat_message WHERE receiver_id = #{userId} AND is_read = 0")
    Integer countUnread(@Param("userId") Long userId);

    @Update("UPDATE bus_chat_message SET is_read = 1 WHERE sender_id = #{senderId} AND receiver_id = #{receiverId}")
    void markAsRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
}
