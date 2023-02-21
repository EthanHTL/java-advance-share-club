package org.example.spring.mvc.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author FLJ
 * @date 2023/2/20
 * @time 16:35
 * @Description handler Mapping tests
 */
public class HandlerMappingTests extends AbstractWebApplicationTests {


    @Configuration
    @EnableWebMvc
    static class MyConfiguration {

        @Bean
        public BeanNameUrlHandlerMapping urlHandlerMapping() {
            return new BeanNameUrlHandlerMapping();
        }

        @Bean("/testbean")
        public Controller testBean() {
            return new Controller() {
                @Override
                public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
                    response.getWriter().write("success, my name is testbean !!!");
                    response.getWriter().close();
                    return null;
                }
            };
        }

    }

    @Override
    protected void registerComponent(AnnotationConfigWebApplicationContext wac) {
        wac.register(MyConfiguration.class);
    }

    @Test
    public void test() throws Exception {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setRequestURI("/testbean");
        HandlerMapping bean = getBean(HandlerMapping.class);

        HandlerExecutionChain handler = bean.getHandler(mockHttpServletRequest);
        Assertions.assertNotNull(handler);
        Assertions.assertSame(getBean("/testbean"),handler.getHandler());

    }

}
