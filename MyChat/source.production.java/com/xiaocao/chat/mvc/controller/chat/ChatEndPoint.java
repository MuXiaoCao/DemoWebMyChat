package com.xiaocao.chat.mvc.controller.chat;

import java.util.Hashtable;
import java.util.Map;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.websocket.HandshakeResponse;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

import org.apache.commons.logging.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.request.SessionScope;

/**
 *	使用WebSocket实现通话的服务端代码 ，实现步骤很简单，如下：
 *	1. 使用ServerEndpoint注解
 *	2. 实现解码器，能够将进入的文本或者二进制消息转换成对象
 *	3. 实现编码器，能够将对象转换成文本或者二进制消息
 *	4. 为了得到session对象，我们需要重写modifyHandshake方法并实现httpSessionListener接口，
 *		在握手的时候该方法将被用来获取http的请求中的session。具体步骤为：
 *		4.1 添加@weblistener接口
 *		4.2 实现httpsessionlistener接口
 *		4.3 生成EndpointConfigurator类，重写modifyHandshake方法，用与得到session对象
 */

@ServerEndpoint(value="/chat/{sessionId}",
		encoders=ChatMessageCode.class,
		decoders=ChatMessageCode.class,
		configurator=ChatEndPoint.EndpointConfigurator.class)
@WebListener
public class ChatEndPoint implements HttpSessionListener{
	
	private static final Logger Log = LogManager.getLogger();
	
	// httpsession对应的key
	private static final String HTTP_SESSION_PROPERTY = "com.xiaocao.websocket.HTTP_SESSION";
	// websocketsession对应的key
	private static final String WEBSOCKET_SESSION_PROPERTY = "com.xiaocao.http.WEBSOCKET_SESSION";
	// 自增长步长
	private static final long sessionIdSequence = 1L;
	// 所对象
	private static final Object sessionIdSequenceLock = new Object();
	// 存放自定义chatSession，相当于所有的聊天室
	private static final Map<Long, ChatSession> chatSessions = new Hashtable<>();
	// 存放所有的WebSocketSession,相当于这个聊天室中的所有用户
	private static final Map<Session, ChatSession> sessions = new Hashtable<>();
	private static final Map<Session, HttpSession> httpSessions = new Hashtable<>();
	
	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
	 *	该类中的方法在握手时被调用，用于暴露底层的http请求，获得session 
	 */
	public static class EndpointConfigurator extends ServerEndpointConfig.Configurator{

		@Override
		public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
			super.modifyHandshake(sec, request, response);
			sec.getUserProperties().put(ChatEndPoint.HTTP_SESSION_PROPERTY, request.getHttpSession());
		}
	}
}
