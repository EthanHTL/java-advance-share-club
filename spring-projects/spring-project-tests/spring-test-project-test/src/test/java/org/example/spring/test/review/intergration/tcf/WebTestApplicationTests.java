package org.example.spring.test.review.intergration.tcf;

import jakarta.servlet.ServletContext;
import org.example.spring.test.review.intergration.tcf.beans.TestBean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.AbstractGenericWebContextLoader;
import org.springframework.test.context.web.WebMergedContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.function.Consumer;

/**
 * @author jasonj
 * @date 2023/12/10
 * @time 16:57
 *
 * @description webApplication 注解的测试
 *
 * {@link org.springframework.test.context.web.WebAppConfiguration} 注解的使用
 *
 *
 * resourcePath 最终将会通过 {@link AbstractGenericWebContextLoader#configureWebResources(GenericWebApplicationContext, WebMergedContextConfiguration)}
 * 来配置ServletContext, 于是在下面的WebMvcConfigurer中,我们仿造了 自动配置来设定了 web app 静态资源的访问策略 ..
 **/
@SpringJUnitWebConfig(resourcePath = "src/test/webapp")
public class WebTestApplicationTests {

    @EnableWebMvc
    // 在spring boot 自动配置中 采用以下配置 来配置了resource handler ..
    // 所以为了结合WebApplication注解的 resourcepath 能够获取到servlet resource
    // 我们需要手动配置一个,因为这里的单元测试没有自动配置 .
//    @Import(WebMvcAutoConfiguration.class)
    @Configuration
    static class Config implements WebMvcConfigurer, ServletContextAware {

        private ServletContext servletContext;

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
//            registry.addResourceHandler("/index.html").addResourceLocations("/index.html");
//            this.addResourceHandler(registry, this.mvcProperties.getWebjarsPathPattern(), "classpath:/META-INF/resources/webjars/");

            this.addResourceHandler(registry, "/**", (registration) -> {
                registration.addResourceLocations("classpath:/META-INF/resources/", "classpath:/resources/", "classpath:/static/", "classpath:/public/");
                if (this.servletContext != null) {
                    ServletContextResource resource = new ServletContextResource(this.servletContext, "/");
                    registration.addResourceLocations(resource);
                }

            });
        }

        private void addResourceHandler(ResourceHandlerRegistry registry, String pattern, String... locations) {
            this.addResourceHandler(registry, pattern, (registration) -> {
                registration.addResourceLocations(locations);
            });
        }

        private void addResourceHandler(ResourceHandlerRegistry registry, String pattern, Consumer<ResourceHandlerRegistration> customizer) {
            if (!registry.hasMappingForPattern(pattern)) {
                ResourceHandlerRegistration registration = registry.addResourceHandler(pattern);
                customizer.accept(registration);
//                registration.setCachePeriod(this.getSeconds(this.resourceProperties.getCache().getPeriod()));
//                registration.setCacheControl(this.resourceProperties.getCache().getCachecontrol().toHttpCacheControl());
//                registration.setUseLastModified(this.resourceProperties.getCache().isUseLastModified());
//                this.customizeResourceHandlerRegistration(registration);
            }
        }

        @Override
        public void setServletContext(ServletContext servletContext) {
            this.servletContext = servletContext;
        }
    }

    @Test
    public void test(@Autowired WebApplicationContext webApplicationContext) throws Exception {
        // pass

        MockMvc build = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();

        build.perform(MockMvcRequestBuilders.get("/index.html"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.TEXT_HTML));
    }
}
