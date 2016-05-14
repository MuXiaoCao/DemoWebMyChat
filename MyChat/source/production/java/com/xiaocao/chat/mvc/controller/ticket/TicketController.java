package com.xiaocao.chat.mvc.controller.ticket;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

/**
 * 有关话题的控制层
 */

@Controller
@RequestMapping("ticket")
public class TicketController {

	private static final Logger log = LogManager.getLogger();
	// 话题自增长步长
	private volatile long TICKET_ID_SEQUENCE = 1;
	// 话题列表
	private Map<Long, Ticket> ticketDatabase = new LinkedHashMap<>();

	/**
	 * 话题默认入口，显示话题列表信息
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = { "", "list" }, method = RequestMethod.GET)
	public String list(Map<String, Object> model) {

		log.debug("Listing tickets.");
		model.put("ticketDatabase", this.ticketDatabase);
		System.out.println("=========================this is ticket list");
		return "ticket/list";
	}

	/**
	 * 显示话题信息
	 * 
	 * @param model
	 * @param ticketId
	 * @return
	 */
	@RequestMapping(value = "view/{ticketId}", method = RequestMethod.GET)
	public ModelAndView view(Map<String, Object> model, @PathVariable("ticketId") long ticketId) {

		Ticket ticket = this.ticketDatabase.get(ticketId);
		// 如果话题不存在就跳转到list页面
		if (ticket == null) {
			return this.getListRedirectModelAndView();
		}
		model.put("ticketId", Long.toString(ticketId));
		model.put("ticket", ticket);
		return new ModelAndView("ticket/view");
	}

	/**
	 * 下载文件
	 * 
	 * @param ticketId
	 * @param name
	 * @return
	 */
	@RequestMapping(value = "/{ticketId}/attachment/{attachment:.+}", method = RequestMethod.GET)
	public View download(@PathVariable("ticketId") long ticketId, @PathVariable("attachment") String name) {

		Ticket ticket = this.ticketDatabase.get(ticketId);
		if (ticket == null) {
			return this.getListRedirectView();
		}
		Attachment attachment = ticket.getAttachment(name);
		if (attachment == null) {
			log.info("Requested attachment {} not found on ticket {}.", name, ticket);
			return this.getListRedirectView();
		}
		return new DownloadingView(attachment.getName(), attachment.getMimeContentType(), attachment.getContents());
	}

	/**
	 * 创建话题的get提交
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "create", method = RequestMethod.GET)
	public String create(Map<String, Object> model) {
		model.put("ticketForm", new Form());
		return "ticket/add";
	}

	/**
	 * 创建话题的post提交
	 * 
	 * @param model
	 * @return
	 * @throws IOException 
	 */
	@RequestMapping(value = "create", method = RequestMethod.POST)
	public View create(HttpSession session, Form form) throws IOException {
		Ticket ticket = new Ticket();
		ticket.setId(this.getNextTicketId());
		ticket.setCustomerName((String) session.getAttribute("username"));
		ticket.setSubject(form.getSubject());
		ticket.setBody(form.getBody());
		ticket.setDateCreated(Instant.now());

		// 处理上传的文件
		for (MultipartFile filePart : form.getAttchments()) {
			log.debug("Processing attachment for new ticket.");
			Attachment attachment = new Attachment();
			attachment.setName(filePart.getOriginalFilename());
			attachment.setMimeContentType(filePart.getContentType());
			attachment.setContents(filePart.getBytes());
			if ((attachment.getName() != null && attachment.getName().length() > 0)
					|| (attachment.getClass() != null && attachment.getContents().length > 0)) {
				ticket.addAttachment(attachment);
			}
		}
		this.ticketDatabase.put(ticket.getId(), ticket);
		return new RedirectView("/ticket/view/" + ticket.getId(),true,false);
	}

	private ModelAndView getListRedirectModelAndView() {
		return new ModelAndView(this.getListRedirectView());
	}

	private View getListRedirectView() {
		return new RedirectView("/ticket/list", true, false);
	}

	private synchronized long getNextTicketId() {
		return this.TICKET_ID_SEQUENCE++;
	}

	public static class Form {
		private String subject;
		private String body;
		private List<MultipartFile> attchments;

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

		public List<MultipartFile> getAttchments() {
			return attchments;
		}

		public void setAttchments(List<MultipartFile> attchments) {
			this.attchments = attchments;
		}

	}
}
