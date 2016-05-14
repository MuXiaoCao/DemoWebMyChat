package com.xiaocao.chat.mvc.controller.ticket;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *	话题类 
 */
public class Ticket {
	// 话题id
	private long id;
	// 话题发出人
	private String customerName;
	// 主题名称
	private String subject;
	// 话题主体
	private String body;
	// 时间
	private Instant dateCreated;
	// 附件列表
	// 此处采用linkedhashmap是因为：不需要线程安全，希望查抄迅速，保存顺序
	private Map<String, Attachment> attachments = new LinkedHashMap<>();

	public void addAttachment(Attachment attachment) {
		this.attachments.put(attachment.getName(), attachment);
	}
	public Attachment getAttachment(String name) {
		return attachments.get(name);
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Instant getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Instant dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Map<String, Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(Map<String, Attachment> attachments) {
		this.attachments = attachments;
	}
	
	
}
