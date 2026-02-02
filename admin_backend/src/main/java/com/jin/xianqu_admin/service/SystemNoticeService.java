package com.jin.xianqu_admin.service;

public interface SystemNoticeService {
    /**
     * 发送系统通知
     * 
     * @param target  "all" 或 用户ID字符串
     * @param content 消息内容
     */
    void sendNotice(String target, String content);
}
