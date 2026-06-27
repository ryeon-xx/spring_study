package org.scoula.config;

import org.springframework.lang.Nullable;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;

public class WebConfig extends AbstractAnnotationConfigDispatcherServletInitializer {

    final String LOCATION = "c:/upload";              // 업로드 한 파일을 저장할 디렉토리 경로
    final long MAX_FILE_SIZE = 1024 * 1024 * 10L;     // 10MB, 파일 하나의 최대 크기
    final long MAX_REQUEST_SIZE = 1024 * 1024 * 20L;  // 20MB, 한 번에 올리는 전체 파일의 최대 크기
    final int FILE_SIZE_THRESHOLD = 1024 * 1024 * 5;  // 5MB, 이 크기보다 작으면 디스크 대신 메모리에서 처리
    // -1 : 크기 제한 없음

    @Nullable
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] { RootConfig.class };
    }

    @Nullable
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] { ServletConfig.class };
    }


    // 스프링의 FrontController인 DispatcherServlet이 담당할 Url 매핑 패턴
    // / : 모든 요청에 대해 매핑
    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }


    // POST body 문자 인코딩 필터 설정 : UTF-8 설정
    protected Filter[] getServletFilters() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();

        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);

        return new Filter[] { characterEncodingFilter };
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        // 처리할 컨트롤러가 없으면 예외를 던지도록 설정
        registration.setInitParameter("throwExceptionIfNoHandlerFound", "true");

        // 파일 업로드 설정
        MultipartConfigElement multipartConfig = new MultipartConfigElement(
                LOCATION,              // 업로드 처리 디렉토리 경로
                MAX_FILE_SIZE,         // 업로드 가능한 파일 하나의 최대 크기
                MAX_REQUEST_SIZE,      // 업로드 가능한 전체 최대 크기(여러 파일 업로드 하는 경우)
                FILE_SIZE_THRESHOLD    // 메모리 파일의 최대 크기(이보다 작으면 실제 메모리에서만 작업)
        );

        registration.setMultipartConfig(multipartConfig);
    }
}
