package com.jin.xianqu_backend.controller.app;

import com.jin.xianqu_backend.common.Result;
import com.jin.xianqu_backend.common.UserContext;
import com.jin.xianqu_backend.model.entity.ChatMessage;
import com.jin.xianqu_backend.model.vo.ChatSessionVO;
import com.jin.xianqu_backend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // 获取会话列表
    @GetMapping("/session")
    public Result<List<ChatSessionVO>> getSessionList() {
        Long userId = UserContext.getUserId();
        if (userId == null)
            return Result.error("未登录");
        return Result.success(chatService.getSessionList(userId));
    }

    // 获取聊天历史
    @GetMapping("/history")
    public Result<List<ChatMessage>> getHistory(
            @RequestParam Long otherUserId,
            @RequestParam(required = false) Long beforeId,
            @RequestParam(defaultValue = "100") int limit) {
        Long userId = UserContext.getUserId();
        if (userId == null)
            return Result.error("未登录");

        List<ChatMessage> list = chatService.getChatHistory(userId, otherUserId, beforeId, limit);
        return Result.success(list);
    }

    // 标记已读
    @PostMapping("/read")
    public Result<Boolean> markAsRead(@RequestBody ChatSessionVO req) {
        Long userId = UserContext.getUserId();
        if (userId == null)
            return Result.error("未登录");

        chatService.markAsRead(userId, req.getOtherUserId());
        return Result.success(true);
    }

    // 获取未读总数
    @GetMapping("/unread")
    public Result<Integer> getUnreadCount() {
        Long userId = UserContext.getUserId();
        if (userId == null)
            return Result.success(0);
        return Result.success(chatService.getUnreadCount(userId));
    }

    // 获取系统通知
    @GetMapping("/system-notice")
    public Result<List<ChatMessage>> getSystemNoticeList() {
        Long userId = UserContext.getUserId();
        System.out.println("DEBUG: getSystemNoticeList called for userId: " + userId);
        return Result.success(chatService.getSystemNoticeList(userId));
    }

    // 标记单条通知已读
    @PostMapping("/read-notice/{id}")
    public Result<Boolean> markNoticeRead(@PathVariable("id") Long id) {
        System.out.println("DEBUG: markNoticeRead called for messageId: " + id);
        chatService.markMessageAsRead(id);
        return Result.success(true);
    }
}
