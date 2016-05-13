package com.xiaocao.chat.config;

import javax.swing.plaf.basic.BasicBorders.MarginBorder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

// 编程式的方式，配置类必须有此注解。
// 此外此注解也是Component的元数据注解，这意味着如果如果启用了组件扫描，
// 标注了Configuration的类将自动读取，这可能会将其中的bean实例化两次
@Configuration
// 设置扫描,设置项有两个，一个是扫描范围，另一个是扫描方式
// 扫描方式为Controller意外的扫描，也就是不扫描controller
@ComponentScan(
		basePackages = "com.xiaocao.chat.mvc",
		excludeFilters = @ComponentScan.Filter(Controller.class)
)
public class RootContextConfiguration {
	
	// 配置类将通过注解了@Bean的无参方法注册bean
	// 由于这里是根应用上下文，所以这里注册到bean将被共享
	
	// 一下的两个bean是用于配置消息转换器的，具体配置在servletContextConfiguration配置类中
	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		// 查找并注册所有的扩展模块
		mapper.findAndRegisterModules();
		// 禁止把日期序列化为时间戳整数，而是序列化为ISO 8901字符串
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,false);
		// 禁止将序列化得到的日期调整为当前时区（这样不含时区的日期字符串被认为是UTC时间）
		mapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
		return mapper;
	}
	
	@Bean
	public Jaxb2Marshaller jaxb2Marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		// 配置扫描XML注解的实体区域
		marshaller.setPackagesToScan(new String[]{"com.xiaocao.chat.mvc"});
		return marshaller;
	}
}
