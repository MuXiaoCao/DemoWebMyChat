package com.xiaocao.chat.mvc.controller.ticket;

/**
 *	话题的附件类
 */
public class Attachment {
	// 附件名称
	private String name;
	// 附件类型
	private String mimeContentType;
	// 附件内容
	private byte[] contents;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMimeContentType() {
		return mimeContentType;
	}

	public void setMimeContentType(String mimeContentType) {
		this.mimeContentType = mimeContentType;
	}

	public byte[] getContents() {
		return contents;
	}

	public void setContents(byte[] contents) {
		this.contents = contents;
	}
	
	
}
