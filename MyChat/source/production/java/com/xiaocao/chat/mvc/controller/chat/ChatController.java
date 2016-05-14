package com.xiaocao.chat.mvc.controller.chat;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 聊天有关的控制层，并不涉及websocket
 */
@Controller
@RequestMapping("chat")
public class ChatController {

	/**
	 * 显示所有在线的聊天室
	 * 
	 * @param mode
	 * @return
	 */
	@RequestMapping(value = "list", method = RequestMethod.GET)
	public String list(Map<String, Object> mode) {
		mode.put("sessions", ChatEndPoint.pendingSession);
		return "chat/list";
	}

	/**
	 * 创建一个新聊天室
	 * 
	 * @param mode
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "new", method = RequestMethod.POST)
	public String newChat(Map<String, Object> mode, HttpServletResponse response) {
		this.setNoCacheHeader(response);
		mode.put("chatSessionId", 0);
		return "chat/chat";
	}

	/**
	 * 加入一个聊天室
	 * @param mode
	 * @param response
	 * @param chatSessionId
	 * @return
	 */
	@RequestMapping(value="join/{chatSessionId}",method=RequestMethod.POST)
	public String joinChat(Map<String, Object> mode,HttpServletResponse response,@PathVariable("chatSessionId") long chatSessionId) {
		this.setNoCacheHeader(response);
		mode.put("chatSessionId", chatSessionId);
		return "chat/chat";
	}

	private void setNoCacheHeader(HttpServletResponse response) {
		response.setHeader("Expires", "Thu, 1 Jan 1970 12:00:00 GMT");
		response.setHeader("Cache-Control", "max-age=0, must-revalidate, no-cache");
		response.setHeader("Pragma", "no-cache");
	}
}
