package com.xiaocao.chat.mvc.controller.chat;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.HandshakeResponse;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * 使用WebSocket实现通话的服务端代码 ，实现步骤很简单，如下： 1. 使用ServerEndpoint注解 2.
 * 实现解码器，能够将进入的文本或者二进制消息转换成对象 3. 实现编码器，能够将对象转换成文本或者二进制消息 4.
 * 为了得到session对象，我们需要重写modifyHandshake方法并实现httpSessionListener接口，
 * 在握手的时候该方法将被用来获取http的请求中的session。具体步骤为： 4.1 添加@weblistener接口 4.2
 * 实现httpsessionlistener接口 4.3
 * 生成EndpointConfigurator类，重写modifyHandshake方法，用与得到session对象
 */

@ServerEndpoint(value = "/chat/{sessionId}", encoders = ChatMessageCode.class, decoders = ChatMessageCode.class, configurator = ChatEndPoint.EndpointConfigurator.class)
@WebListener
public class ChatEndPoint implements HttpSessionListener {

	private static final Logger Log = LogManager.getLogger();

	// httpsession对应的key
	private static final String HTTP_SESSION_PROPERTY = "com.xiaocao.websocket.HTTP_SESSION";
	// websocketsession对应的key
	private static final String WEBSOCKET_SESSION_PROPERTY = "com.xiaocao.http.WEBSOCKET_SESSION";
	// 自增长步长
	private static long sessionIdSequence = 1L;
	// 所对象
	private static final Object sessionIdSequenceLock = new Object();
	// 存放chatSession的id与chatSession的对应关系
	private static final Map<Long, ChatSession> chatSessions = new Hashtable<>();
	// 存放所有的WebSocketSession,相当于这个聊天室中的所有用户
	private static final Map<Session, ChatSession> sessions = new Hashtable<>();
	// 每个websocketSession与httpsession对应关系存储
	private static final Map<Session, HttpSession> httpSessions = new Hashtable<>();
	// 存放当前所有的聊天室,需要前台显示，所以为public
	public static final List<ChatSession> pendingSession = new ArrayList<>();

	/**
	 * 创建或者加入一个聊天室
	 * @param session	websocket的session
	 * @param sessionId 如果小于1 表示希望创建聊天室 否则表示希望加入的聊天室id
	 */
	@OnOpen
	public void OnOpen(Session session, @PathParam("sessionId") long sessionId) {
		Log.entry(sessionId);
		// 通过session得到httpsession
		HttpSession httpSession = (HttpSession) session.getUserProperties().get(ChatEndPoint.HTTP_SESSION_PROPERTY);
		try {
			// 判断是否为非法用户
			if (httpSession == null || httpSession.getAttribute("username") == null) {
				Log.warn("Attempt to access chat server while logged out.");
				session.close(new CloseReason(
                        CloseReason.CloseCodes.VIOLATED_POLICY,
                        "You are not logged in!"));
				return;
			}
			// 向websocket的session中注册httpsession的username参数
			String username = (String) httpSession.getAttribute("username");
			session.getUserProperties().put("username", username);
			
			// 封装消息对象（共有四部分）
			ChatMessage message = new ChatMessage();
			// 1.封装时间戳：offsetdatatime是java1.8的API，表示当前的日期
			message.setTimestamp(OffsetDateTime.now());
			// 2.封装用户名
			message.setUser(username);
			// 封装类型和内容
			ChatSession chatSession;
			
			if (sessionId < 1) {
				Log.debug("User starting chat {} is {}.",sessionId,username);
				// 3.封装消息类型：如果sessionId<1表示需要创建聊天室的消息
				message.setType(ChatMessage.Type.STARTED);
				// 4.封装消息内容：创建聊天室的消息内容是固定的
				message.setContent(username + " started the chat session");
				
				// 生成聊天室对象
				chatSession = new ChatSession();
				// 对新生成的聊天室对象初始化
				synchronized (ChatEndPoint.sessionIdSequenceLock) {
					chatSession.setSessionId(ChatEndPoint.sessionIdSequence++);
				}
				chatSession.setCustomer(session);
				chatSession.setCustomerUsername(username);
				chatSession.setCreationMessage(message);
				
				// 加入聊天室列表
				ChatEndPoint.pendingSession.add(chatSession);
				// 更新聊天室id和对象列表
				ChatEndPoint.chatSessions.put(chatSession.getSessionId(), chatSession);
			}else { //如果sessionid>=1 ,则表示是加入聊天室
				Log.debug("User joining chat {} is {}.",sessionId,username);
				// 封装消息类型和内容
				message.setType(ChatMessage.Type.JOINED);
				message.setContent(username + " joined the chat session");
				// 从聊天室id 聊天室对象列表中，通过id获得聊天室对象
				chatSession = ChatEndPoint.chatSessions.get(sessionId);
				chatSession.setRepresentative(session);
				chatSession.setRepresentativeUsername(username);
				// 加入成功之后，需要删除聊天列表，因为聊天列表显示的是当前可选的聊天室，每个聊天室只允许两个人进入
				ChatEndPoint.pendingSession.remove(chatSession);
				// 通过websocket发送消息
				session.getBasicRemote().sendObject(chatSession.getCreationMessage());
				session.getBasicRemote().sendObject(message);
			}
			
			// 更新session和聊天室列表
			ChatEndPoint.sessions.put(session, chatSession);
			// 更新session和httpsession列表
			ChatEndPoint.httpSessions.put(session, httpSession);
		} catch (Exception e) {
			this.OnError(session, e);
		}finally {
			Log.exit();
		}
	}
	
	/**
	 * 发送信息
	 * @param session
	 * @param message
	 */
	@OnMessage
	public void OnMessage(Session session,ChatMessage message) {
		Log.entry();
		// 通过用户的session得到聊天室对象
		ChatSession c = ChatEndPoint.sessions.get(session);
		Session other = this.getOtherSession(c, session);
		if (c != null && other != null) {
			// 记录聊天日志
			c.log(message);
			try {
				// 向双方发送消息
				session.getBasicRemote().sendObject(message);
				other.getBasicRemote().sendObject(message);
			} catch (Exception e) {
				this.OnError(session, e);
			}
		}else {
			Log.warn("Chat message received with only one chat member.");
		}
		Log.exit();
	}
	
	
	@OnClose
	public void OnClose(Session session,CloseReason reason) {
		if (reason.getCloseCode() == CloseReason.CloseCodes.NORMAL_CLOSURE) {
			ChatMessage message = new ChatMessage();
			message.setUser((String)session.getUserProperties().get("username"));
			message.setType(ChatMessage.Type.LEFT);
			message.setTimestamp(OffsetDateTime.now());
			message.setContent(message.getUser() + " left the chat.");
			try {
				Session other = this.close(session, message);
				if (other != null) {
					other.close();
				}
			} catch (Exception e) {
				Log.warn("Problem closing companion chat session.", e);
	
			}
		}else {
            Log.warn("Abnormal closure {} for reason [{}].", reason.getCloseCode(),
                    reason.getReasonPhrase());
	
		}
	}
	@OnError
	public void OnError(Session session,Throwable e) {
		Log.warn("Error received in WebSocket session.", e);
        ChatMessage message = new ChatMessage();
        message.setUser((String)session.getUserProperties().get("username"));
        message.setType(ChatMessage.Type.ERROR);
        message.setTimestamp(OffsetDateTime.now());
        message.setContent(message.getUser() + " left the chat due to an error.");
        try {
            Session other = this.close(session, message);
            if(other != null)
                other.close(new CloseReason(
                        CloseReason.CloseCodes.UNEXPECTED_CONDITION, e.toString()
                ));
        }
        catch(IOException ignore) { }
        finally{
            try
            {
                session.close(new CloseReason(
                        CloseReason.CloseCodes.UNEXPECTED_CONDITION, e.toString()
                ));
            }
            catch(IOException ignore) { }
            Log.exit();
        }
	}
	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		HttpSession httpSession = event.getSession();
		Log.entry(httpSession.getId());
        if(httpSession.getAttribute(WEBSOCKET_SESSION_PROPERTY) != null)
        {
            ChatMessage message = new ChatMessage();
            message.setUser((String)httpSession.getAttribute("username"));
            message.setType(ChatMessage.Type.LEFT);
            message.setTimestamp(OffsetDateTime.now());
            message.setContent(message.getUser() + " logged out.");
            for(Session session:new ArrayList<>(this.getSessionsFor(httpSession)))
            {
            	Log.info("Closing chat session {} belonging to HTTP session {}.",
                        session.getId(), httpSession.getId());
                try
                {
                    session.getBasicRemote().sendObject(message);
                    Session other = this.close(session, message);
                    if(other != null)
                        other.close();
                }
                catch(IOException | EncodeException e)
                {
                	Log.warn("Problem closing companion chat session.");
                }
                finally
                {
                    try
                    {
                        session.close();
                    }
                    catch(IOException ignore) { }
                }
            }
        }
	}
	
	/**
	 * 关闭一个用户session
	 * @param s
	 * @param message
	 * @return
	 */
	private Session close(Session s,ChatMessage message) {
		Log.entry();
		ChatSession c = ChatEndPoint.sessions.get(s);
		Session other = this.getOtherSession(c, s);
		ChatEndPoint.sessions.remove(s);
		HttpSession h = ChatEndPoint.httpSessions.get(s);
		if (h != null) {
			this.getSessionsFor(h).remove(s);
		}
		if (c != null) {
			c.log(message);
			ChatEndPoint.pendingSession.remove(c);
			ChatEndPoint.chatSessions.remove(c.getSessionId());
			try {
				c.writeChatLog(new File("chat." + c.getSessionId() + ".log"));
			} catch (Exception e) {
				Log.error("Could not write chat log due to error.",e);
			}
		}
		if (other != null) {
			ChatEndPoint.sessions.remove(other);
			h = ChatEndPoint.httpSessions.get(other);
			if (h!=null) {
				this.getSessionsFor(h).remove(s);
			}
			try {
				other.getBasicRemote().sendObject(message);
			} catch (Exception e) {
				Log.warn("Prolem closing companion chat session.",e);
			}
		}
		return Log.exit(other);
	}
	
	@SuppressWarnings("unchecked")
	private synchronized ArrayList<Session> getSessionsFor(HttpSession session) {
		Log.entry();
		try {
			if (session.getAttribute(WEBSOCKET_SESSION_PROPERTY)==null) {
				session.setAttribute(WEBSOCKET_SESSION_PROPERTY, new ArrayList<>());
			}
			return (ArrayList<Session>) session.getAttribute(WEBSOCKET_SESSION_PROPERTY);
		} catch (Exception e) {
			return new ArrayList<>();
		}finally {
			Log.exit();
		}
	}

	/**
	 * 通过session返回聊天室对象中的另一个用户的session
	 * @param c
	 * @param s
	 * @return
	 */
	private Session getOtherSession(ChatSession c,Session s) {
		Log.entry();
		return Log.exit(c == null ? null:
			(s == c.getCustomer()? c.getRepresentative():c.getCustomer()));
	}
	
	/**
	 * 该类中的方法在握手时被调用，用于暴露底层的http请求，获得session
	 */
	public static class EndpointConfigurator extends ServerEndpointConfig.Configurator {

		@Override
		public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
			Log.entry();
			super.modifyHandshake(sec, request, response);
			sec.getUserProperties().put(ChatEndPoint.HTTP_SESSION_PROPERTY, request.getHttpSession());
			Log.exit();
		}
	}

	@Override
	public void sessionCreated(HttpSessionEvent event) {
	}
}
