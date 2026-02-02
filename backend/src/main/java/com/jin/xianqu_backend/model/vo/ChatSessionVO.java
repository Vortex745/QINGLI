package com.jin.xianqu_backend.model.vo;

import lombok.Builder;
import lombok.Data;
import java.util.Date;

@Data
@Builder
public class ChatSessionVO {
    private Long otherUserId;
    private String otherUserName;
    private String otherUserAvatar;
    private String lastContent;
    private Date lastTime;
    private Integer unreadCount;
}
