package org.example.spring.test.integration.support.each;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.aopalliance.aop.Advice;
import org.junit.jupiter.api.Test;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.context.request.*;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.ServletRequest;

/**
 * 测试请求以及session作用域的bean
 *
 * 只需如下几步,确保webApplicationContext 被正确加载(@WebAppConfiguration)
 * 注册mock 请求和session 到测试实例中并准备好测试装置 ..
 * 执行从ctx中获取的web 组件(使用依赖注入)
 * 根据mocks执行断言
 *
 */
public class TestRequestAndSessionScopedTests {
    @Data
    static class LoginAction {
        private  String user;

        private  String password;


    }

    @Data
    @AllArgsConstructor
    public static class UserPreference {

        private String theme;
    }

    @Data
    public static class SessionPreference {
        private String name;
    }

    @SpringJUnitWebConfig
    public static class RequestScopedBeanTests {

        /**
         * 这里使用的是c命名空间,进行构造器注入
         */
        @Configuration
        @ImportResource(locations = {"classpath:configs/sessionScopeTestConfig.xml"})
        static class ReqeustScopedConfig {

            @org.springframework.web.context.annotation.RequestScope
            @Bean
            public LoginAction loginAction() {

                return new LoginAction() {
                    @Override
                    public String getPassword() {
                        ServletWebRequest request1 = (ServletWebRequest) RequestContextHolder.getRequestAttributes();
                        return request1.getParameter("password");
                    }

                    @Override
                    public String getUser() {
                        ServletWebRequest request1 = (ServletWebRequest) RequestContextHolder.getRequestAttributes();
                        return request1.getParameter("user");
                    }
                };
            }

            /**
             * 这可以的原因是,它底层使用了 ScopedProxyFactoryBean 来创建作用域代理
             *
             * 然后结合 ioc#getBean方法实现对scope bean的实时创建 ...(根据不同scope 规则创建不同的真正的bean)
             * @param theme
             * @return
             */
            @SessionScope
            @Bean
            public SessionPreference sessionPreference(@Value("#{session.getAttribute('theme')}") String theme) {
                return new SessionPreference() {{
                    setName(theme);
                }};
            }
        }


        @Autowired
        LoginAction loginAction;

        @Autowired
        MockHttpServletRequest request;
        @Autowired
        MockHttpSession session;

        @Autowired
        UserPreference userPreference;

        @Autowired
        ApplicationContext context;

        @Autowired
        SessionPreference sessionPreference;

        @Test
        public void test() {
            request.setParameter("user", "enigma");
            request.setParameter("password", "$pr!ng");
            System.out.println(loginAction.getPassword());
            System.out.println(loginAction.getUser());
        }

        @Test
        public void sessionTest() {
            session.setAttribute("theme","blue");

            /**
             * 需要通过方法调用,因为jdk动态代理是代理接口方法的 ...
             */
            System.out.println(userPreference.getTheme());
//            Object bean = context.getBean("scopedTarget.org.example.spring.test.integration.support.each.TestRequestAndSessionScopedTests$UserPreference#0");
//            System.out.println(bean);

            session.setAttribute("theme","red");

            System.out.println(userPreference.getTheme());
        }

        @Test
        public void annotationSessionTest() {
            session.setAttribute("theme","blank");

            System.out.println(sessionPreference.getName());
        }


    }
}
