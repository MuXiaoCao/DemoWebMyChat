package com.xiaocao.chat.mvc.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.swing.text.View;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AuthenticationController {
	private static final Logger log = LogManager.getLogger();
	private static final Map<String, String> userDatabase = new HashMap<>();
	
	static {
		userDatabase.put("xiaocao", "123");
		userDatabase.put("xiaohua", "1234");
		userDatabase.put("xiaobai", "123456");
		userDatabase.put("xiaoli", "12345678");
	}
	
	@RequestMapping("logout")
	public View logout(HttpSession session) {
		if (log.isDebugEnabled()) {
			
		}
	}
}
