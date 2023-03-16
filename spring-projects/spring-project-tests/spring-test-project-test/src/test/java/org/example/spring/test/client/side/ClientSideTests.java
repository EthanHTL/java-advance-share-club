package org.example.spring.test.client.side;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.test.web.client.MockMvcClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.ExecutingResponseCreator;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * 客户端 测试 支持 ..
 */
public class ClientSideTests {

    @Test
    public void test() {
        RestTemplate restTemplate = new RestTemplate();

        MockRestServiceServer serviceServer = MockRestServiceServer.bindTo(restTemplate).build();

        // 期待有一个 /greeting 请求
        serviceServer.expect(requestTo("/greeting")).andRespond(withSuccess("greeting", MediaType.APPLICATION_JSON));

        ResponseEntity<String> template = restTemplate.getForEntity("/greeting", String.class);
        Assertions.assertNotNull(template);

        // 给出了一个mock response ..
//        System.out.println(template.getBody());

        // 测试內部使用RestTemplate的代码 ...
        Assertions.assertEquals(template.getBody(),"greeting");

        // 这样请求过程被自动 mock ... 我们仅仅关注 业务逻辑即可 ..


        serviceServer.verify();
    }

    @Test
    public void orderForManyTimes() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restTemplate).build();

        mockServer.expect(times(2), requestTo("/something")).andRespond(withSuccess());
        mockServer.expect(times(3), requestTo("/somewhere")).andRespond(withSuccess());

        // 正常处理,first
        restTemplate.getForEntity("/something", Void.class);
        restTemplate.getForEntity("/somewhere", Void.class);
        restTemplate.getForEntity("/somewhere", Void.class);

        // 不关心顺序
        restTemplate.getForEntity("/something", Void.class);
        restTemplate.getForEntity("/somewhere", Void.class);

        mockServer.verify();
    }


    /**
     * 但是需要spring 6.0 ,目前版本不是 6.0
     */
    @Test
    public  void forRealRequestAndMockResponse() {
        RestTemplate restTemplate = new RestTemplate();

        int index = -1;
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for (int i = 0; i < messageConverters.size(); i++) {
            if (messageConverters.get(i) instanceof StringHttpMessageConverter) {
                index = i;
                break;
            }
        }

        // 解决 StringHttpMessageConverter 字符集编码问题 ...
        if(index >= 0) {
            messageConverters.add(index,new StringHttpMessageConverter(StandardCharsets.UTF_8));
            messageConverters.remove(index + 1);
        }

// Create ExecutingResponseCreator with the original request factory
        ExecutingResponseCreator withActualResponse = new ExecutingResponseCreator(restTemplate.getRequestFactory());

        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        mockServer.expect(requestTo("/profile")).andRespond(withSuccess());
        mockServer.expect(requestTo("https://www.baidu.com")).andRespond(withActualResponse);

// Test code that uses the above RestTemplate ...

        ResponseEntity<String> entity = restTemplate.getForEntity("/profile", String.class);
        ResponseEntity<String> forEntity = restTemplate.getForEntity("https://www.baidu.com", String.class);
        Assertions.assertNotNull(forEntity);

        Assertions.assertNotNull(forEntity.getBody());

        System.out.println(forEntity.getBody());

        mockServer.verify();
    }
}
