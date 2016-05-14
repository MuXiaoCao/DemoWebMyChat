package com.xiaocao.chat.mvc.controller.session;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("session")
public class SessionListController {
	@RequestMapping(value="list",method=RequestMethod.GET)
	public String list(Map<String, Object> mode) {
		mode.put("timestamp", System.currentTimeMillis());
		mode.put("numberOfSession", SessionRegistry.getNumberOfSessions());
		mode.put("sessionList", SessionRegistry.getAllSessions());
		
		return "session/list";
	}
}
