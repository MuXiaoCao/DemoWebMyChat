package com.xiaocao.chat.mvc.controller.chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 为ChatEndpoint提供解码器和编码器，其中需要指定一个消息类
 */
public class ChatMessageCode implements Encoder.BinaryStream<ChatMessage>, Decoder.BinaryStream<ChatMessage> {

	private static final Logger Log = LogManager.getLogger();
	private static final ObjectMapper MAPPER = new ObjectMapper();

	static {
		// 查找并注册所有的扩展模块
		MAPPER.findAndRegisterModules();
		// 关闭自动关闭模式
		MAPPER.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
	}
	
	/**
	 * 解码器：将is流中的json对象转化为chatmessage对象
	 */
	@Override
	public ChatMessage decode(InputStream is) throws DecodeException, IOException {
		Log.entry();
		try {
			return ChatMessageCode.MAPPER.readValue(is, ChatMessage.class);
		} catch (Exception e) {
			throw new DecodeException((ByteBuffer)null, e.getMessage(),e);
		} finally {
			Log.exit();
		}
	}
	
	/**
	 * 编码器：将chatmessage对象经过mapper转化为json对象
	 */
	@Override
	public void encode(ChatMessage chatMessage, OutputStream os) throws EncodeException, IOException {
		Log.entry();
		try {
			ChatMessageCode.MAPPER.writeValue(os, chatMessage);
		} catch (Exception e) {
			throw new EncodeException(chatMessage, e.getMessage(),e.getCause());
		} finally {
			Log.exit();
		}

	}
	
	@Override
	public void init(EndpointConfig config) {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
}
