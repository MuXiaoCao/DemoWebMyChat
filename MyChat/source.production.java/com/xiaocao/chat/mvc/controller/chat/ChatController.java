package com.xiaocao.chat.mvc.controller.chat;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("caht")
public class ChatController {
	
	@RequestMapping(value="list", method=RequestMethod.GET)
	public String list(Map<String, Object> mode) {
		
	}
	
}
