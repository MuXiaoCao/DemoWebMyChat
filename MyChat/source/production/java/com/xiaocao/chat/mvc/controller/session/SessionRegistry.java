package com.xiaocao.chat.mvc.controller.session;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * 用于管理注册过的session集合，包含方法有：
 * 1. addSession
 * 2. updateSessionId
 * 3. removeSession
 * 4. getAllSessions
 * 5. getNumberOfSessions
 */
public final class SessionRegistry {
	
	/**
	 * 这里使用hashtable存储session是考虑到sessions是公共的资源需要引入同步机制，
	 * 而一般用的hashmap是不保证同步到
	 * 另外，hashtable与hashmap的不同有：
	 * 1. 前者的key之多可以一个为null，后者不可以为null
	 * 2. 前者没有contains方法，之后containsKey和containsValue方法。后者之后contains方法
	 * 3. 前者线程不安全，后者线程安全，因此后者的效率会慢一些
	 * 4. 前者使用iterator遍历，后者使用Enumeration
	 * 5. 前者初始数组大小为11，增加的方式是old*2+1.后者默认大小是16，而且一定是2的指数
	 * 6. 前者中的hash值是通过换算的，后者直接用的hashcode
	 */
	private static final Map<String, HttpSession> SESSIONS = new Hashtable<>();
	
	/**
	 *  添加session 
	 */
	public static void addSession(HttpSession session) {
		SESSIONS.put(session.getId(), session);
	}
	
	/**
	 *	更新session的id 
	 */
	public static void updateSessionId(HttpSession session) {
		synchronized (SESSIONS) {
			SESSIONS.remove(session);
			addSession(session);
		}
	}
	
	/**
	 *	删除session 
	 */
	public static void removeSession(HttpSession session) {
		SESSIONS.remove(session);
	}
	
	/**
	 *	返回session列表 
	 */
	public static List<HttpSession> getAllSessions() {
		return new ArrayList<>(SESSIONS.values());
	}
	
	/**
	 *	返回当前sessions列表的大小，即用户数量 
	 */
	public static int getNumberOfSessions() {
		return SESSIONS.size();
	}
	
	private SessionRegistry() {
		
	}
}
