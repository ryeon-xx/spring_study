package org.scoula.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice  // 모든 컨트롤러의 예외를 공통 처리
// @ControllerAdvice :
//  - HTTP 상태코드 500 Internal Server Error에 대응하기 위한 기법
//  - AOP (Aspect-Oriented-Programming)을 이용
//  - org.scoula.exception.CommonExceptionAdvice.java
@Log4j2
public class CommonExceptionAdvice {

    // 스프링 MVC의 예외 처리
    //  - @ExceptionHandler와 @ControllerAdvice를 이용한 처리
    //  - @ResponseEntity를 이용하는 예외 메시지 구성
    @ExceptionHandler(Exception.class)  // Exception 종류의 예외를 잡음
    public String except(Exception ex, Model model) {
        log.error("Exception....." + ex.getMessage());
        model.addAttribute("exception", ex); // 예외 정보를 화면에 전달

        log.error(model);
        return "error_page";  // 오류 화면으로 연결
    }


    // 404 에러 페이지
    //  - 404 에러는 서버에서 Exception을 발생시키지 않음
    //    ▪ 예외 클래스 : NoHandlerFoundException
    //  - 404 에러를 Exception으로 처리하려면 설정 필요
    //    ▪ Advice에서 NoHandlerFoundException 예외 처리
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handle404(NoHandlerFoundException ex, Model model, HttpServletRequest request) {
        log.error(ex);

        model.addAttribute("uri", request.getRequestURI());

        return "custom404";
    }
}
