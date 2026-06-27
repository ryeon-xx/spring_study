package org.scoula.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;


@EnableWebMvc

// @ComponentScan : Spring MVC용 컴포넌트 등록을 위한 스캔 패키지
@ComponentScan(basePackages = {
        "org.scoula.controller",
        "org.scoula.exception"  // 예외 처리 클래스가 있는 패키지 추가
})
public class ServletConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/resources/**")  // url이 /resources/로 시작하는 모든 경로
                .addResourceLocations("/resources/");            // webapp/resources/ 경로로 매핑
    }

    /*
        controller에서 index라는 view 이름을 리턴한 경우
        /WEB-INF/views/ + view 이름 + .jsp
         -> /WEB-INF/views/home.jsp
     */
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        InternalResourceViewResolver bean = new InternalResourceViewResolver();
        bean.setViewClass(JstlView.class);
        bean.setPrefix("/WEB-INF/views/");
        bean.setSuffix(".jsp");
        registry.viewResolver(bean);
    }


    // Servlet 3.0 파일 업로드 사용시 - MultipartResolver 빈 등록
    @Bean
    // 멀티파트 요청을 처리하는 담당자를 빈으로 등록
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();

        return resolver;
    }
}
