package com.darklands.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/*
* 这段代码的作用是：后端告诉浏览器，“允许来自 5174 和 5173 的前端跨域访问我”。
*
* @Configuration：告诉 Spring 这是一个配置类，启动时要加载它。
implements WebMvcConfigurer：实现 Spring MVC 的扩展接口。
addCorsMappings(...) 是 Spring MVC 在启动时回调的方法，你在这里注册跨域规则。
这个类要放在启动类所在包或子包下，否则组件扫描不到。


* */

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")//表示这些跨域规则应用到所有路径：
                /*允许的来源*/
                .allowedOrigins("http://localhost:5174", "http://localhost:5173")
                /*允许的方法*/
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}