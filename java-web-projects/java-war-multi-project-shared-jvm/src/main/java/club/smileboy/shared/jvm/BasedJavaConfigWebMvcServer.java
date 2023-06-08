package club.smileboy.shared.jvm;

import jakarta.servlet.Filter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import java.nio.charset.StandardCharsets;

/**
 * 采用 servlet 3.0 容器启动  监听器注册方式 ..
 */
public class BasedJavaConfigWebMvcServer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[0];
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{
                WebConfig.class
        };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    // 增加过滤器
    @Override
    protected Filter[] getServletFilters() {
        return new Filter[] {
                new CharacterEncodingFilter(StandardCharsets.UTF_8.name()),
                new HiddenHttpMethodFilter()
        };
    }
}
