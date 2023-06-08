package org.example.spring.mvc.test;

import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.FormContentFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Map;

/**
 * 表单内容的早期解析单元测试
 *
 * 确定是否真的是 早期解析
 *
 * 有关真实示例的测试,请查看
 */
@SpringJUnitWebConfig
public class FormContentEarlyResolveTests {

    @Configuration
    @EnableWebMvc
    public static class Config {

        @Bean
        public MyController myController() {
            return new MyController();
        }
    }

    private static MockMvc mockMvc;

    @BeforeEach
    public void setup(WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
    }
    @Test
    public void test() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/")
                .content("key=value").contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(MockMvcResultMatchers.content().string("key=value"));
    }

    @RestController
    @RequestMapping("/")
    public static class MyController {

        @PutMapping
        public String toMap(@RequestParam Map<String,Object> requestParams) {
            return "key=" + requestParams.getOrDefault("key","unknown");
        }
    }


    @Nested
    class WithFormContentResolveTests {

        @Configuration
        public static class Config {

            /**
             * 并没有使用它 ..
             * @return
             */
            @Bean
            public FilterRegistrationBean<FormContentFilter> formContentFilterFilterRegistrationBean() {
                return new FilterRegistrationBean<>(
                        new FormContentFilter()
                );
            }
        }

        /**
         * 使用的是{@link org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder#buildRequest(ServletContext)}
         * 中有关 {@link MediaType#APPLICATION_FORM_URLENCODED}的处理 ..
         * @throws Exception
         */
        @Test
        public void test() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.put("/")
                            .content("key=value").contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(MockMvcResultMatchers.content().string("key=value"));

        }
    }
}
