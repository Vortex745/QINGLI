package com.jin.xianqu_backend.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.jin.xianqu_backend.model.entity.ChatMessage;
import com.jin.xianqu_backend.service.ChatService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/{userId}")
@Component
@Slf4j
public class WebSocketServer {

    // Static service injection
    private static ChatService chatService;

    @Autowired
    public void setChatService(ChatService chatService) {
        WebSocketServer.chatService = chatService;
    }

    // Thread-safe map to store sessions
    private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    private String userId;
    private Session session;

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.session = session;
        this.userId = userId;
        if (sessionMap.containsKey(userId)) {
            sessionMap.remove(userId);
            // Optionally close old session
        }
        sessionMap.put(userId, session);
        log.info("用户连接: {}, 当前在线人数: {}", userId, sessionMap.size());
    }

    @OnClose
    public void onClose() {
        if (sessionMap.containsKey(userId)) {
            sessionMap.remove(userId);
        }
        log.info("用户断开: {}, 当前在线人数: {}", userId, sessionMap.size());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到消息: user={}, msg={}", userId, message);

        try {
            JSONObject msgObj = JSON.parseObject(message);
            String action = msgObj.getString("action"); // "chat" or "ping"

            if ("ping".equals(action)) {
                sendMessage(session, "{\"type\":\"pong\"}");
                return;
            }

            if ("chat".equals(action)) {
                // Parse chat fields
                Long toUserId = msgObj.getLong("toUserId");
                String content = msgObj.getString("content");
                Integer type = msgObj.getInteger("msgType"); // 0=text, 1=img, 2=goods
                Long goodsId = msgObj.getLong("goodsId");

                // 1. Save to DB
                ChatMessage savedMsg = chatService.saveMessage(Long.valueOf(userId), toUserId, content, type, goodsId);

                // 2. Push to Receiver if online
                Session receiverSession = sessionMap.get(String.valueOf(toUserId));
                if (receiverSession != null && receiverSession.isOpen()) {
                    // Send full message object
                    sendMessage(receiverSession, JSON.toJSONString(savedMsg));
                }
            }
        } catch (Exception e) {
            log.error("消息处理异常", e);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket Error: user=" + userId, error);
    }

    private void sendMessage(Session session, String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }
}
