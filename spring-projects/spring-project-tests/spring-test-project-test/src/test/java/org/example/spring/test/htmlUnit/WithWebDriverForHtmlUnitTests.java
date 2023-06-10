package org.example.spring.test.htmlUnit;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import jakarta.servlet.http.HttpServletResponse;
import org.assertj.core.api.Assertions;
import org.example.spring.test.htmlUnit.pages.AbstractPage;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder;
import org.springframework.test.web.servlet.htmlunit.webdriver.MockMvcHtmlUnitDriverBuilder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.io.IOException;

/**
 * @author jasonj
 * @date 2023/6/10
 * @time 08:52
 * @description 这里使用selenium 的WebDriver 来进一步封装HtmlUnit  通过使用 Page 对象模式
 *
 *
 * spring-htmlunit-test <a href="https://github.com/spring-attic/spring-test-htmlunit/blob/master/mail-webapp/src/test/java/sample/webdriver/pages/CreateMessagePage.java">spring-htmlunit-test</a>
 **/

@SpringJUnitWebConfig(resourcePath = "src/test/webapp")
public class WithWebDriverForHtmlUnitTests {


    @Configuration
    @EnableWebMvc
    public static class Config implements WebMvcConfigurer {


        @Bean
        public TestController testController() {
            return new TestController();
        }


        @Override
        public void configureViewResolvers(ViewResolverRegistry registry) {
            registry.viewResolver(new InternalResourceViewResolver("/", ".html"));
        }

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/login.html").addResourceLocations("/login.html");
            // 不给任何协议前缀, 交由 ServletContext 解析资源 ..
            //最终就是ServletContextResource ..
            registry.addResourceHandler("/login-success.html").addResourceLocations("/login-success.html");
        }
    }


    @Controller
    public static class TestController {

        @GetMapping("login")
        public String login() {
            return "login";
        }

        /**
         * 在直接通过 htmlUnit 测试的时候, 可以没有这个方法,那么 返回的状态码就是 400
         *
         * 那么我们将统一 不管是由 接口导致或者没有接口所导致的错误归结为 login failure ..
         *
         * 以便断言 ...
         *
         * {@link #useNormalHtmlUnitPageObjectPattern()}
         */
        @PostMapping("login")
        public void login(String username, String password, HttpServletResponse response) throws IOException {
            response.setStatus(HttpStatus.OK.value());
            if (username.equals("user") && password.equals("password")) {
                response.setContentType(MediaType.TEXT_HTML_VALUE);
                response.getOutputStream().write("login success".getBytes());
            } else {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getOutputStream().write("login failure".getBytes());
            }
        }
    }


    private WebApplicationContext webApplicationContext;

    private HtmlUnitDriver webDriver;

    private WebClient webClient;

    public WithWebDriverForHtmlUnitTests(WebApplicationContext webApplicationContext) {
        this.webApplicationContext = webApplicationContext;


        this.webDriver = MockMvcHtmlUnitDriverBuilder.webAppContextSetup(webApplicationContext).build();
        this.webClient = MockMvcWebClientBuilder.webAppContextSetup(webApplicationContext).build();
    }


    public static class LoginPage {

        final HtmlPage currentPage;

        final HtmlTextInput userName;

        final HtmlPasswordInput password;

        final HtmlSubmitInput submit;

        public LoginPage(HtmlPage currentPage) {
            this.currentPage = currentPage;
            this.userName = currentPage.getHtmlElementById("username");
            this.password = currentPage.getHtmlElementById("password");
            this.submit = currentPage.getHtmlElementById("submit");
        }


        public void setUserName(String username) {
            this.userName.setText(username);
        }

        public void setPassword(String password) {
            this.password.setText(password);
        }


        public LoginResponsePage login() {
            try {
                Page click = submit.click();
                return new LoginResponsePage(click, null);
            } catch (Exception e) {
                return new LoginResponsePage(null, "login failure");
            }
        }
    }

    public static class LoginResponsePage {

        @Nullable
        final Page resultPage;

        @Nullable
        final String errorMessage;

        final boolean isLoginSuccess;

        @Nullable
        final String successMessage;


        public LoginResponsePage(@Nullable Page resultPage, @Nullable String errorMessage) {
            this.resultPage = resultPage;
            this.errorMessage = errorMessage != null ? errorMessage : loginErrorMessage();
            this.isLoginSuccess = isLoginSuccessCheck();

            this.successMessage = loginSuccessMessage();
        }

        public boolean isLoginSuccessCheck() {
            return !StringUtils.hasText(errorMessage) && isPage();
        }

        public String loginSuccessMessage() {
            if (isPage()) {
                assert resultPage != null;
                HtmlPage resultPage1 = (HtmlPage) resultPage;
                return resultPage1.getBody().getTextContent();
            }
            return null;
        }

        public String loginErrorMessage() {
            if (isPage()) {
                return null;
            } else if (resultPage != null) {
                UnexpectedPage resultPage1 = (UnexpectedPage) resultPage;

                System.out.println("login failure for parse");
                return resultPage1.getWebResponse().getContentAsString().trim();
            }
            return "login failure";
        }


        public boolean isLoginSuccess() {
            return isLoginSuccess;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public boolean isPage() {
            return resultPage != null && resultPage.isHtmlPage();
        }

        public String getSuccessMessage() {
            return successMessage;
        }
    }

    @Test
    public void useNormalHtmlUnitPageObjectPattern() throws IOException {
        Page page = webClient.getPage("http://localhost/login");

        LoginPage loginPage = new LoginPage(((HtmlPage) page));

        loginPage.setUserName("user");
        loginPage.setPassword("guest");

        LoginResponsePage loginResponsePage = loginPage.login();

        if (!loginResponsePage.isLoginSuccess) {

            // 没有登录成功, 打印错误消息
            Assertions.assertThat(loginResponsePage.errorMessage).isEqualTo("login failure");
        } else {
            // 打印登录成功信息
            Assertions.assertThat(loginResponsePage.getSuccessMessage()).isEqualTo("login success");
        }

    }

    public static class DriverLoginPage extends AbstractPage {

        /**
         * 没有注解的默认按照 name / id 进行元素注入 ..
         * <p>
         * 详情查看 {@link  org.openqa.selenium.support.pagefactory.Annotations}
         */

        WebElement username;

        WebElement password;

        @FindBy(css = "#submit")
        WebElement submit;


        public DriverLoginPage(WebDriver driver) {
            super(driver);
        }


        public static DriverLoginPage of(WebDriver webDriver) {
            get(webDriver, "/login");
            return PageFactory.initElements(webDriver, DriverLoginPage.class);
        }

        public DriverLoginResponsePage login() {
           return  login("user","password");
        }

        public DriverLoginResponsePage login(String usernameStr,String passwordStr) {
            username.sendKeys(usernameStr);
            password.sendKeys(passwordStr);

            submit.click();

            return DriverLoginResponsePage.of(driver);
        }

        public static class DriverLoginResponsePage extends AbstractPage {


            private WebElement body;

            private Page page;

            public DriverLoginResponsePage(WebDriver driver) {
                super(driver);

                this.page = ((HtmlUnitDriver) driver).getCurrentWindow().getWebWindow().getTopWindow().getEnclosedPage();
            }


            public static DriverLoginResponsePage of(WebDriver webDriver) {
                return PageFactory.initElements(webDriver, DriverLoginResponsePage.class);
            }


            public boolean isLoginSuccess() {
                if (page != null && page.isHtmlPage()) {
                    if (page.getWebResponse().getContentAsString().trim().equals("login success")) {

                        return true;
                    }
                }
                return false;
            }

            public String getErrorMessage() {
                if (!isLoginSuccess()) {
                    return page.getWebResponse().getContentAsString().trim();
                }

                return null;
            }
        }
    }


    /**
     * 直接通过WebDriver 实现 页面数据的抓取 ...
     *
     * 但是由于通过{@link PageFactory#initElements(SearchContext, Class)}所代理的所有{@link WebElement} 字段都是基于代理实现的,
     * 它每次都可能会从webDriver中获取元素,这就导致, 创建的Page 无法重用,因为driver 随时根据页面数据变化 ..
     *
     *
     * 所以下述代码通过 重新创建来实现 相同页面的多次重试 ..
     *
     * 或者让webDriver 回到上一个页面,也能够重用 ..
     */
    @Test
    public void upgradeForWebDriverUseHtmlUnit() {
        DriverLoginPage loginPage = DriverLoginPage.of(webDriver);
        DriverLoginPage.DriverLoginResponsePage login = loginPage.login();

        org.junit.jupiter.api.Assertions.assertTrue(login.isLoginSuccess());

        DriverLoginPage loginPage1 = DriverLoginPage.of(webDriver);
        DriverLoginPage.DriverLoginResponsePage responsePage = loginPage1.login("user", "guest");
        org.junit.jupiter.api.Assertions.assertFalse(responsePage.isLoginSuccess());
        org.junit.jupiter.api.Assertions.assertEquals(responsePage.getErrorMessage(),"login failure");
    }
}
