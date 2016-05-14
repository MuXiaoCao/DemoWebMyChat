package com.xiaocao.chat.config;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * servlet上下文配置类
 * 
 * @author 小草
 *
 */

// 配置类注解
@Configuration
// 激活注解驱动的控制器请求映射
@EnableWebMvc
// 配置扫描方式
// 关闭默认扫描方式，采用白名单方式，只扫描controller
@ComponentScan(basePackages = {"com.xiaocao.chat.mvc.controller.*","com.xiaocao.chat.mvc.controller"}, useDefaultFilters = false, includeFilters = @ComponentScan.Filter(Controller.class) )
/**
 * 返回响应实体 1. 配置消息转换器 2. 配置内容协商 3. 在controller中使用@ResponseBody,返回对象 4.
 * 在返回的类中使用对应注解 作用：通过消息转换器可以将响应的数据转换为控制器方法可以处理的某种类型的java对象
 * controller处理后返回的对象，通过内容协商生成指定的响应格式
 */
// 消息转换器必须继承WebMvcConfigurerAdapter并重写configureMessageConverters
public class ServletContextConfiguration extends WebMvcConfigurerAdapter {

	// 1. 配置消息转换器
	// 注入rootContextConfiguration中的bean
	@Inject
	ObjectMapper objectMapper;
	@Inject
	Marshaller marshaller; // 后两个其实是一个
	@Inject
	Unmarshaller unmarshaller;

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

		// 一下四种是所有的Spring会自动配置的转换器，这里将按照他们通常出现的顺序进行配置
		// 顺序很重要，因为有的转换器有更宽的MIME类型和Java类型范围，这可能会屏蔽我们希望使用的转换器
		converters.add(new ByteArrayHttpMessageConverter());
		converters.add(new StringHttpMessageConverter());
		converters.add(new FormHttpMessageConverter());
		converters.add(new SourceHttpMessageConverter<>());

		// 以下是用于支持xml实体的转换器
		MarshallingHttpMessageConverter xmlConverter = new MarshallingHttpMessageConverter();
		xmlConverter.setSupportedMediaTypes(
				Arrays.asList(new MediaType("application", "xml"), new MediaType("text", "xml")));
		xmlConverter.setMarshaller(this.marshaller);
		xmlConverter.setUnmarshaller(this.unmarshaller);
		converters.add(xmlConverter);

		// 以下是用于支持json实体的转换器
		MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
		jsonConverter.setSupportedMediaTypes(
				Arrays.asList(new MediaType("application", "json"), new MediaType("text", "json")));
		jsonConverter.setObjectMapper(this.objectMapper);
		converters.add(jsonConverter);
	}

	// 2. 配置内容协商
	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		// 该配置将启用文件扩展名检查，禁用请求参数检查，确保Accept头不会被忽略。
		// 禁用了jaf（java activation
		// framework），它可以将文件扩展名映射到媒体类型的工具。我们将采用手动指定可用的媒体类型
		// 最后将默认的类型设置为xml，还添加了对xml和json的支持
		configurer.favorPathExtension(true).favorParameter(false).parameterName("mediaType").ignoreAcceptHeader(false)
				.useJaf(false).defaultContentType(MediaType.APPLICATION_XML).mediaType("xml", MediaType.APPLICATION_XML)
				.mediaType("json", MediaType.APPLICATION_JSON);
	}

	// 配置视图解析
	// 如果是显示视图，在controller中只需要返回视图名称即可，在这将添加视图名称的前缀和后缀
	@Bean
	public ViewResolver viewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setViewClass(JstlView.class);
		resolver.setPrefix("/WEB-INF/jsp/view/");
		resolver.setSuffix(".jsp");
		System.out.println("======================this is viewResolver");
		return resolver;
	}

	// 文件上传部分：通过该实例告诉spring使用servlet 3.0+ 还是使用一些第三方工具实现文件上传
	@Bean
	public MultipartResolver multipartResolver() {
		return new StandardServletMultipartResolver();
	}

	@Bean
	public RequestToViewNameTranslator viewNameTranslator() {
		return new DefaultRequestToViewNameTranslator();
	}

}
