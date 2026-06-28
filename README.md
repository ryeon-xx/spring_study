# 순수 Spring MVC 뼈대 + @Log4j2 흐름 추적 가이드

> 스택: Gradle / JDK 17 / Tomcat 9 (Servlet 4.0, javax 패키지) / Spring 5.3.x / Java Config / Lombok @Log4j2

> 컨셉: web.xml 없이 `WebApplicationInitializer`로 컨테이너를 직접 띄우고, Root Context와 Servlet Context를 분리해서 "왜 분리하는지"를 로그로 직접 눈으로 확인한다.

---

## 0. 전체 프로젝트 구조

```
spring-mvc-skeleton
 ├─ build.gradle
 └─ src
     └─ main
         ├─ java
         │   └─ com/example
         │       ├─ config
         │       │   ├─ WebAppInitializer.java   (Tomcat 진입점, web.xml 대체)
         │       │   ├─ RootConfig.java          (Root Context: service, repository)
         │       │   └─ ServletConfig.java       (Servlet Context: controller, viewResolver)
         │       ├─ controller
         │       │   └─ HelloController.java
         │       ├─ service
         │       │   ├─ HelloService.java
         │       │   └─ HelloServiceImpl.java
         │       └─ repository
         │           ├─ HelloRepository.java
         │           └─ HelloRepositoryImpl.java
         ├─ resources
         │   └─ log4j2.xml                       (로그 레벨/출력 형식 설정)
         └─ webapp
             └─ WEB-INF
                 └─ views
                     └─ hello.jsp
```

**왜 Root Context / Servlet Context를 나누는가?**
나중에 MyBatis를 붙이면 `SqlSessionFactory`, `DataSource`, `@Repository`, `@Service`처럼 "DB·비즈니스 영역"은 여러 서블릿(혹은 Security 필터 등)이 공유해야 한다. 반면 `@Controller`, `ViewResolver`는 "웹 요청·응답 영역"이라 DispatcherServlet 전용이다. 이 둘을 같은 컨테이너에 다 때려넣으면 나중에 구조가 꼬인다. 그래서:
- `RootConfig` → service, repository만 스캔 (ContextLoaderListener가 띄움, 여러 서블릿이 공유)
- `ServletConfig` → controller만 스캔 (DispatcherServlet이 띄움, 이 서블릿 전용)
- 둘은 부모-자식 관계라서 **자식(Servlet Context)은 부모(Root Context)의 빈을 주입받을 수 있다.** (반대는 불가능)

---
## 1. Spring MVC 라이프 사이클

> #### 처리 순서 :
> Filter -> DispatcherServlet -> HandlerMapping -> HandlerInterceptor -> Controller
> -> Service -> Repository(Mapper) -> ViewResolver 순
 
> #### Filter
> 
> - Web Application의 전역적인 로직을 담당
> - Filter라는 단어 뜻에서 알 수 있듯, 전체적인 필터링(설정)을 하는 곳
> - DispatcherServlet에 들어가기 전인 Web Application 단에서 실행

> #### DispatcherServlet
> 
> - 들어오는 모든 Request를 우선적으로 받아 처리해주는 서블릿
> - HandlerMapping에게 Request에 대해 매핑할 Controller 검색을 요청
> - HandlerMapping으로부터 Controller 정보를 반환받아 해당 Controller와 매핑
> 
>   → Request에 대해 어느 컨트롤러로 매핑시킬 것인지 배치하는 역할

> #### HandlerMapping
> 
> - DispatcherServlet으로부터 검색을 요청받은 Controller를 찾아 정보를 리턴

> #### HandlerInterceptor
> 
> - Request가 Controller 매핑되기 전 앞 단에서 부가적인 로직을 추가
> - 주로 세션, 쿠키, 권한 인증 로직에 많이 사용됩니다.
 
> #### Controller
> 
> - Request와 매핑되는 곳
> - Request에 대해 어떤 로직(Service) 으로 처리할 것인지를 결정
> - 그에 맞는 Service를 호출
> - Service Bean을 스프링 컨테이너로부터 주입

> #### Service
> 
> - 데이터 처리 및 가공을 위한 비즈니스 로직을 수행
> - Request에 대한 실질적인 로직을 수행
> - Repository를 통해 DB에 접근하여 데이터의 CRUD(Create, Read, Update, Delete)를 처리

> #### Repository (DAO, Data Access Object)
> 
> - "DB에 접근하는 객체" 라고 부릅니다.
> - Service에서 DB에 접근할 수 있게 하여 데이터의 CRUD 처리

> #### ViewResolver
> 
> - Controller에서 리턴한 View의 이름을 DispatcherServlet으로부터 넘겨받고,
> - 해당 View로 forward
>
>   → 이후, 해당 렌더링 된 View 화면은 브라우저로 전송 (response)


---

## 2. Security Filter Chain

> #### Security ContextPersistenceFilter
> 
> - request가 발생하면 SecurityContext 객체의 생성, 저장, 조회를 담당하는 필터
> - 새로운 SecurityContext를 생성하여 SecurityContextHolder에 저장
> - 익명의 사용자의 경우
>   - AnonymousAuthenticationFilter에서 AnonymousAuthenticationToken 객체를 SecurityContext에 저장
> - 인증 시
>   - UsernamePasswordAuthenticationFilter에서 인증 성공 후 SecurityContext에
> 
>     UsernamePasswordAuthentication객체를 Authentication객체와 함께 저장
>   - 인증이 완료되면 Session에 SecurityContext를 저장하고 응답함
> - 인증 후
>   - Session에서 SecurityContext를 꺼내 SecurityContextHolder에 저장
>   - SecurityContext 내 Authentication 객체가 있으면 인증을 유지
 
> #### LogoutFilter
> 
> - 유저의 로그아웃을 진행
> - 설정된 로그아웃 URL로 오는 요청을 감시하며, 해당 유저를 로그아웃 처리

> #### UsernamePasswordAuthenticationFilter
> 
> - 설정된 로그인 URL로 오는 요청을 감시하며, 유저 인증을 처리
> - 인증 실패 시, AuthenticationFailureHandler를 실행

> #### DefaultLoginPageGeneratingFilter
> 
> - 사용자가 별도의 로그인 페이지를 구현하지 않은 경우, 기본적으로 설정한 로그인 페이지를 처리

> #### BasicAuthenticationFilter
> 
> - HTTP요청의 (BASIC)인증 헤더를 처리하여 결과를 SecurityContextHolder에 저장

> #### RememberMeAuthenticationFilter
> 
> - SecurityContext에 인증(Authentication) 객체가 있는지 확인
> - RememberMeServices를 구현한 객체의 요청이 있을 경우, Remember-Me 인증 토큰으로 컨텍스트에 주입

> #### AnonymousAuthenticationFilter
> 
> - SecurityContextHolder에 인증(Authentication) 객체가 있는지 확인
> - 필요한 경우 Authentication 객체를 주입

> #### SessionManagementFilter
> 
> - 요청이 시작된 이후 인증된 사용자인지 확인하고, 인증된 사용자일 경우, SessionAuthenticationStrategy를 호출하여
>   세션 고정 보호 매커니즘을 활성화하거나 여러 동시 로그인을 확인하는 것과 같은 세션 관련 활동을 수행
> - 다른 브라우저에 이미 로그인한 경우, 여러 브라우저에서 로그인 불가 처리

> #### ExceptionTranslationFilter
> 
> - 필터 체인 내에서 발생하는 모든 예외(AccessDeniedException, AuthenticationException)를 처리

> #### FilterSecurityInterceptor
> 
> - HTTP 리소스의 보안처리를 수행