package org.scoula.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
// @Configuration : 이 클래스가 스프링 설정을 담는 클래스임을 표시
@PropertySource({"classpath:/application.properties"})
// @PropertySource({"properties 경로 문자열"})
//  ▪ 클래스 레벨 어노테이션
//  ▪ 사용할 .properties 경로를 지정
//  ▪ 예 : @PropertySource({ "classpath:/application.properties" })
@ComponentScan(basePackages = {
        "org.scoula"
})
// @MapperScan(basePackages = {  })
// @MapperScan
//  ▪ Mapper 인터페이스를 검색할 패키지 목록 지정
//  ▪ 해당 인터페이스를 빈으로 등록
//  ▪ 해당 인터페이스의 구현체를 동적으로 자동 생성
public class RootConfig {

    @Autowired
    ApplicationContext applicationContext;

    // @Value("${키:기본값}")
    //  ▪ 필드 레벨 어노테이션
    //  ▪ 기본값은 생략 가능
    //  ▪ 예 : @Value("${jdbc.driver}") String driver;
    @Value("${jdbc.driver}") String driver;
    @Value("${jdbc.url}") String url;
    @Value("${jdbc.username}") String username;
    @Value("${jdbc.password}") String password;


    // 빈(Bean) : 스프링이 직접 생성하고 관리하는 객체
    @Bean
    // 이 메소드가 돌려주는 객체를 컨텍스트에 빈으로 등록
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        HikariDataSource dataSource = new HikariDataSource(config);

        return dataSource;
    }

    @Bean
    // SQLSession : Connection 생성, SQL 전달, 결과 리턴 등 처리
    // SQLSessionFactory : SQLSession 객체 생성
    public SqlSessionFactory sqlSessionFactory(ApplicationContext applicationContext) throws Exception {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setConfigLocation(applicationContext.getResource("classpath:/mybatis-config.xml"));
        sqlSessionFactory.setDataSource(dataSource());

        return (SqlSessionFactory) sqlSessionFactory.getObject();
    }


    @Bean
    public DataSourceTransactionManager transactionManager() {
        DataSourceTransactionManager manager = new DataSourceTransactionManager(dataSource());

        return manager;
    }
}
