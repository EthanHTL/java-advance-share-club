package org.example.spring.test.example;

import jakarta.persistence.Column;
import org.hibernate.annotations.Comment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

/**
 * mock Mvc 示例 测试
 */
public class MockMvcExampleTests {

    /**
     * 对controller 进行单元测试
     * 但是测试 不包含 一些额外的功能支持 ..
     *
     */
    @SpringJUnitWebConfig
    public static class WithControllerForMockMvcTests {

        @Configuration
        @RequestMapping("/api/login")
        @RestController
        public static class Config {

            @GetMapping
            public String login() {
                return "login";
            }
        }

        private MockMvc mockMvc;

        @BeforeEach
        public void beforeEach(WebApplicationContext webApplicationContext) {
           this.mockMvc = MockMvcBuilders.standaloneSetup(webApplicationContext.getBean(Config.class))
                   // default 请求能够为所有请求定制一些公共的配置设置 ..
                   .defaultRequest(MockMvcRequestBuilders.get("/api").param("default","defaultValue"))
                   .build();
        }

        @Test
        public void test() throws Exception {

            this.mockMvc.perform(MockMvcRequestBuilders.get("/api/login"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andDo(MockMvcResultHandlers.log());

        }
    }


}
