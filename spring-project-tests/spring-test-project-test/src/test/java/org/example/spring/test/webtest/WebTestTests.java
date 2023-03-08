package org.example.spring.test.webtest;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

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
}
