package org.example.spring.test.review.intergration.htmlUnit;

import com.gargoylesoftware.htmlunit.WebClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author jasonj
 * @date 2023/12/14
 * @time 23:08
 * @description
 **/
@RunWith(SpringRunner.class)
@WebAppConfiguration
public class WebTestWithHtmlUnitTests {

    @Configuration
    static class Config {

    }
    @Test
    public void test(@Autowired WebApplicationContext applicationContext) {
        WebClient webClient = MockMvcWebClientBuilder.webAppContextSetup(applicationContext)
                .build();

        // 返回一个页面
//        webClient.getPage()
    }
}
