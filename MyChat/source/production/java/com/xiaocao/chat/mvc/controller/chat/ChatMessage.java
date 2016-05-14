package com.xiaocao.chat.mvc.controller.chat;

import java.time.OffsetDateTime;

/**
 *	聊天信息封装类
 */
public class ChatMessage {
	// 时间戳
	private OffsetDateTime timestamp;
	// 消息类型，共5个（started：开始  joined：加入  error：错误  left:离开   text：聊天文本）
	private Type type;
	// 发送用户
	private String user;
	// 内容
	private String content;
	
	
	public OffsetDateTime getTimestamp() {
		return timestamp;
	}



	public void setTimestamp(OffsetDateTime timestamp) {
		this.timestamp = timestamp;
	}



	public Type getType() {
		return type;
	}



	public void setType(Type type) {
		this.type = type;
	}



	public String getUser() {
		return user;
	}



	public void setUser(String user) {
		this.user = user;
	}



	public String getContent() {
		return content;
	}



	public void setContent(String content) {
		this.content = content;
	}



	public static enum Type {
        STARTED, JOINED, ERROR, LEFT, TEXT
    }
}
