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