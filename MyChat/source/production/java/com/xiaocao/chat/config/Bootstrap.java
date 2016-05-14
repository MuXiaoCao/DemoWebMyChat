package com.xiaocao.chat.config;

import javax.servlet.FilterRegistration;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.xiaocao.chat.mvc.filter.AuthenticationFilter;
import com.xiaocao.chat.mvc.filter.LogginFilter;

/**
 * 通过桥接口WebApplicationInitializer的onStartup实现编程式的配置web.xml
 * 
 * @author 小草
 *
 */
public class Bootstrap implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext container) throws ServletException {
		// 设置默认路径
		container.getServletRegistration("default").addMapping("/resource/*");

		// 配置根应用上下文
		// 得到注解配置解析器
		AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
		// 将自定义的根配置类注册到解析器
		rootContext.register(RootContextConfiguration.class);
		// 再将解析器绑定到容器的监听器中
		container.addListener(new ContextLoaderListener(rootContext));

		// 配置servlet应用上下文
		// 同样得到注册配置解析器
		AnnotationConfigWebApplicationContext servletContext = new AnnotationConfigWebApplicationContext();
		// 将自定义的servlet配置类注册到解析器
		servletContext.register(ServletContextConfiguration.class);
		// 得到servlet应用上下文dispatcher,并绑定到容器中，同时设置配置类为servletContext
		ServletRegistration.Dynamic dispatcher = container.addServlet("springDispatcher",
				new DispatcherServlet(servletContext));
		// 配置启动方式为第一次访问加载
		dispatcher.setLoadOnStartup(1);
		// 配置文件上传参数
		dispatcher.setMultipartConfig(new MultipartConfigElement(null, 20_971_520L, 41_943_040L, 512_000));
		// 配置url路径
		dispatcher.addMapping("/");

		// 配置监听
		// 注册日志监听
		FilterRegistration.Dynamic registration = container.addFilter("loggingFilter", new LogginFilter());
		// 配置url路径
		registration.addMappingForUrlPatterns(null, false, "/*");

		// 注册权限监听
		registration = container.addFilter("authenticationFilter", new AuthenticationFilter());
		// 配置url路径
		registration.addMappingForUrlPatterns(null, false, "/ticket", "/ticket/*", "/chat", "/chat/*", "/session",
				"/session/*");
	}

}
