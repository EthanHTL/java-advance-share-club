package org.example.spring.test.htmlUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * @author jasonj
 * @date 2023/6/9
 * @time 17:53
 *
 * @description 通过 使用htmlUnit 结合 springSecurity mockMvc 来测试页面 ..
 *
 *
 * 这和 MockMvc 的测试有所区别, 从单个交互,变为了流程测试 ..
 *
 * 从处理每一个请求变为 处理流程测试 ..
 **/
@SpringJUnitWebConfig(resourcePath = "src/test/webapp")
public class WithSpringSecurityForMockMvcTests {


    @Configuration
    @EnableWebMvc
    @EnableMethodSecurity
    @EnableWebSecurity
    /**
     * 由于先解析 import ,然后再解析自己的propertySource .. 所以在这里导入的属性可能在import 配置类中无法得到 ..
     */
    public static class Config implements WebMvcConfigurer  {

        @Override
        public void configureViewResolvers(ViewResolverRegistry registry) {
            registry.viewResolver(new InternalResourceViewResolver("/",".html"));
        }

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/index.html").addResourceLocations("/index.html");
        }

        @Bean
        public SecurityFilterChain httpSecurity(HttpSecurity httpSecurity) throws Exception {

            return httpSecurity.authorizeHttpRequests()
                    .requestMatchers("/json")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
                    .and()
                    .formLogin()
                    .successHandler((request, response, authentication) -> {
                        response.sendRedirect("/index.html");
                    })
                    .and()
                    .build();
        }

        @Bean
        public TestController testController() {
            return new TestController();
        }

        /**
         * 手动提供一个 用户详情服务
         */
        @Bean
        public UserDetailsService userDetailsService() {
            return new InMemoryUserDetailsManager(
                    User.builder().username("user").password("{noop}guest").authorities(Collections.emptyList()).build()
            );
        }

    }

    @RestController
    public static class TestController {

        @GetMapping(value = "json",produces = MediaType.APPLICATION_JSON_VALUE)
        public Object json() throws JsonProcessingException {
            return new ObjectMapper().writeValueAsString(Map.of("k1","v1","k2","v2"));
        }
    }

//    @Autowired
//    private UserDetailsService userDetailsService;


    private WebClient webClient;


    @BeforeEach
    public void setUp(WebApplicationContext applicationContext) {

        webClient = MockMvcWebClientBuilder.webAppContextSetup(applicationContext,springSecurity()).build();
    }



    @Test
    public void loginPageTest() throws IOException {
        Page page = webClient.getPage("http://localhost/");
        if (page.isHtmlPage()) {
            HtmlPage htmlPage = (HtmlPage) page;
            // check csrf
            DomNode domNode = htmlPage.getDocumentElement().querySelector("input[name='_csrf']");
            assertThat(domNode.getTextContent()).isNotNull();

            //        Assertions.assertThat(userDetailsService).isNotNull();


            // 此时没有自动配置 UserDetailsService ,那么 我们尝试提交表单


            Page click = loginReturnPage(htmlPage,"user","guests");

            HtmlPage responsePage = (HtmlPage) click;

            HtmlElement documentElement = responsePage.getDocumentElement();
            DomNode domNode1 = documentElement.querySelector("div.alert-danger");

            HtmlDivision division = (HtmlDivision) domNode1;

            String textContent = division.getTextContent();
            assertThat(textContent).isNotNull();
        }
    }

    private static Page loginReturnPage(HtmlPage htmlPage,String usernameStr,String passwordStr) throws IOException {
        HtmlElement document = htmlPage.getDocumentElement();

        DomNode userName = document.querySelector("input[id='username']");
        DomNode password = document.querySelector("input[id='password']");

        ((HtmlTextInput) userName).setText(usernameStr);

        ((HtmlPasswordInput) password).setText(passwordStr);
        DomNode form = document.querySelector("form.form-signin");
        HtmlForm htmlForm = (HtmlForm) form;

        HtmlButton htmlSubmitInput = (HtmlButton) document.querySelector("button[type='submit']");

        return htmlSubmitInput.click();
    }


    @Test
    public void forJsonResponseWithHtmlUnit() throws IOException {
        Page page = webClient.getPage("http://localhost/json");

        assertThat(page.isHtmlPage()).isFalse();

        WebResponse webResponse = page.getWebResponse();
        assertThat(webResponse.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);

        // page is UnExpectPage ..
        assertThat(webResponse.getContentAsString()).isEqualTo("{\"k1\":\"v1\",\"k2\":\"v2\"}");

    }


    @Test
    public void forLoginSuccess() throws IOException {
        HtmlPage page = (HtmlPage) webClient.getPage("http://localhost");
        Page returnPage = loginReturnPage(page, "user", "guest");

        assertThat(returnPage.isHtmlPage()).isTrue();


        HtmlPage htmlPage = (HtmlPage) returnPage;

        DomNode domNode = htmlPage.getDocumentElement().querySelector("div.tip");
        String textContent = domNode.getTextContent();

        Assertions.assertThat(textContent.trim()).isEqualTo("login success");
    }

}
