package com.xiaocao.chat.mvc.controller.chat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 *	存放session的类 
 */
public class ChatSession {
	private static final Logger log = LogManager.getLogger();
	
	// SessionId
	private long sessionId;
	// 用户名
	private String customerUsername;
	// websocket的senssion对象
	private Session customer;
	// 代表的用户名
	private String representativeUsername;
	// 代表的session
	private Session representative;
	// 聊天消息
	private ChatMessage creationMessage;
	// 聊天记录
	private final List<ChatMessage> chatLog = new ArrayList<>();
	public long getSessionId() {
		return sessionId;
	}
	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}
	public String getCustomerUsername() {
		return customerUsername;
	}
	public void setCustomerUsername(String customerUsername) {
		this.customerUsername = customerUsername;
	}
	public Session getCustomer() {
		return customer;
	}
	public void setCustomer(Session customer) {
		this.customer = customer;
	}
	public String getRepresentativeUsername() {
		return representativeUsername;
	}
	public void setRepresentativeUsername(String representativeUsername) {
		this.representativeUsername = representativeUsername;
	}
	public Session getRepresentative() {
		return representative;
	}
	public void setRepresentative(Session representative) {
		this.representative = representative;
	}
	public ChatMessage getCreationMessage() {
		return creationMessage;
	}
	public void setCreationMessage(ChatMessage creationMessage) {
		this.creationMessage = creationMessage;
	}
	
	/**
	 *	存储聊天信息方法 
	 */
	@JsonIgnore
	public void log(ChatMessage message) {
		log.trace("chat message logged for session().",this.sessionId);
		this.chatLog.add(message);
	}
	
	/**
	 * 将聊天记录写入指定文件
	 * @param file
	 * @throws IOException
	 */
	@JsonIgnore
	public void writeChatLog(File file) throws IOException {
		log.debug("Writing chat log to file {}.",file);
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		try (FileOutputStream stream = new FileOutputStream(file)){
			mapper.writeValue(stream, this.chatLog);
		} 
		log.exit();
	}
}
