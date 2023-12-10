package org.example.spring.test.review.util.mvc.servletapi;

import net.minidev.json.JSONUtil;
import org.example.spring.test.review.util.AbstractUseJsonTests;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.Arrays;

/**
 * @author jasonj
 * @date 2023/10/29
 * @time 20:15
 * @description
 **/
@SpringJUnitWebConfig
public class UseServletApiTests extends AbstractUseJsonTests {

    private static MockMvc mvc;

    /**
     * 需要启动 WebMvc 来实现对 一些响应解析器的注入 ...
     *
     * 否则直接使用,则可能有一些MediaType 无法解析出来 ..
     */
    @Configuration
//    @EnableWebMvc
    @RequestMapping("/api/user")
    @RestController
    static class SimpleController {


        @GetMapping
        public SimplePojo parseUser(SimplePojo simplePojo) {
                return simplePojo;
        }
    }

    @BeforeAll
    static void beforeAll(WebApplicationContext applicationContext) {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }


    @Test
    public void requestWithResponseTest() throws Exception {

        SimplePojo pojo = new SimplePojo();
        pojo.setUsername("百度");
        pojo.setPassword("234");
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/user")
//                .content(asJson(pojo))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .queryParam("username", "百度")
                .queryParam("password", "234");

        mvc.perform(requestBuilder)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content()
                        .json(asJson(pojo)));

    }



}
