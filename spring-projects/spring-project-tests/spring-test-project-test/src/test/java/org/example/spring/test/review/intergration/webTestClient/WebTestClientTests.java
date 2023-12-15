package org.example.spring.test.review.intergration.webTestClient;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * @author jasonj
 * @date 2023/12/13
 * @time 23:45
 * @description
 **/
public class WebTestClientTests {

    @Test
    public void test() {

        //
        WebTestClient client = WebTestClient.bindToServer().build();
        // 返回的是流式exchange结果 可以能够测试text-stream 等相关无限流 或者 sse 时间
        FluxExchangeResult<String> exchangeResult = client.get().exchange()
                .returnResult(String.class);

        // 由于使用的是Flux
        // 则可以通过StepVerifier 来进行验证
        Flux<String> responseBody = exchangeResult.getResponseBody();



    }

    @Test
    public void mockMvcResultTests() {
        WebTestClient client = WebTestClient.bindToServer().build();
        // 返回的是流式exchange结果 可以能够测试text-stream 等相关无限流 或者 sse 时间
        EntityExchangeResult<String> exchangeResult = client.get().exchange()
                .expectBody(String.class)
                .returnResult();
        // 基于mock mvc的形式校验结果
        ResultActions resultActions = MockMvcWebTestClient.resultActionsFor(exchangeResult);
    }


    @Test
    public void stepVerifierTests() {
        Flux<Integer> just = Flux.just(1, 2, 3, 4);

        StepVerifier.create(just)
                .expectNextCount(4)
                .expectComplete()
                .verify();
    }
}

