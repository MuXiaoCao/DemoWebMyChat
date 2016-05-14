package com.xiaocao.chat.mvc.filter;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

public class LogginFilter implements Filter {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		Logger log = LogManager.getLogger();
		log.info(((HttpServletRequest) request).getRequestURL());
		ThreadContext.put("id", UUID.randomUUID().toString());
		HttpSession session = ((HttpServletRequest) request).getSession();
		if (session != null) {
			ThreadContext.put("username", (String) session.getAttribute("username"));
		}
		try {
			chain.doFilter(request, response);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			log.info("this is logginFilter finally");
			ThreadContext.clearAll();
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

}
