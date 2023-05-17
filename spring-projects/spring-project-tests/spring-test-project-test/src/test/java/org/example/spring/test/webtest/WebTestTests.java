package org.example.spring.test.webtest;

import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class WebTestTests {
    @Test
    public void test() {

        WebTestClient.bindToServer()
                .baseUrl("https://www.baidu.com")
                .build()
                .get()
                .exchange()
                .expectBody(String.class)
                .value(System.out::println);

    }

    @Test
    public void testController() {
        WebTestClient.bindToController(new MyController())
                .configureClient()
                .build()
                .get()
                .uri(UriComponentsBuilder.fromUriString("/api/test").queryParam("name","jasonj").build().toUri())
                .exchange()
                .expectBody(String.class)
                .value(System.out::println);
    }

    @RestController
    @RequestMapping("/api/test")
    public static class MyController {
        @Setter
        private MYAService myaService;
        @GetMapping
        public String value() {
            return "12312";
        }


        @GetMapping("/v1")
        public String value1(String name) {
            if (myaService != null) {
                myaService.valueOf();
            }
            return name;
        }

    }

    public static class MYAService {

        void valueOf() {
            System.out.printf("12312");
        }
    }


    @SpringJUnitWebConfig
    public static class BaseWebTests {

        @Configuration
        public static class Config {

            @Bean
            public MyController myController() {
                return new MyController();
            }


            @Bean
            public MYAService myaService() {
                MYAService mock = Mockito.mock(MYAService.class);
                myController().setMyaService(mock);
                return mock;
            }

        }

        @Autowired
        private MyController myController;

        private MockMvc mockMvc;

        @BeforeEach
        public void each(WebApplicationContext context) {
            this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        }

        @Test
        public void test() throws Exception {
//           myController.value1("123");
mockMvc.perform(MockMvcRequestBuilders.get("/api/test/v1").param("name","123"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(MockMvcResultHandlers.log());
        }
    }
}
