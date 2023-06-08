package club.smileboy.shared.jvm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

@EnableWebMvc
@ComponentScan
@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.stream().filter(ele -> ele instanceof StringHttpMessageConverter)
                .forEach(ele -> {
                    ((StringHttpMessageConverter) ele).setDefaultCharset(StandardCharsets.UTF_8);
                });
    }
}
