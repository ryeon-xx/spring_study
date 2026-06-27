package org.scoula.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
public class HomeController {

    @GetMapping("/")
    public String index() {
        log.info("================> HomeController 매핑 /");

        return "index";  // View의 이름
    }
}

/*
    Model로 뷰에 데이터 전달
      Model로 값 전달하기
       : 컨트롤러에서 처리한 데이터를 화면(JSP)으로 전달하려면 Model 사용
         addAttribute(이름, 값) 으로 데이터 담아서 해당 값이 화면으로 전달.
         서블릿에서 request.setAttribute()로 값을 담아 JSP로 전달하던 작업을 대체하는 방식.
      JSP에서 ${담은 이름} 형태로 값 사용

    기본 자료형 값도 화면에 전달하고 싶다면 @ModelAttribute("이름") 붙임
    그러면 그 값이 지정한 이름으로 화면에 전달

    @GetMapping("/")
    public String index(Model model) {
        log.info("================> HomeController 매핑 /");
        model.addAttribute("name", "홍길동");

        return "index";  // View의 이름
    }
 */