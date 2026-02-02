package com.jin.xianqu_admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 聊天消息实体
 */
@TableName("bus_chat_message")
@Data
public class ChatMessage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long senderId;

    private Long receiverId;

    private String content;

    /**
     * 消息类型: 0-文本, 1-图片, 2-商品卡片, 9-系统通知
     */
    private Integer type;

    private Long goodsId;

    /**
     * 是否已读: 0-未读, 1-已读
     */
    private Integer isRead;

    private Date createTime;

    private static final long serialVersionUID = 1L;
}
