package club.smileboy.app.common;

import club.smileboy.app.common.deferred.DeferedValueV2Selector;
import club.smileboy.app.common.imported.MyImportBeanDefinitionRegistrar;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
@Import({DeferedValueV2Selector.class, MyImportBeanDefinitionRegistrar.class})
@Configuration
public class SecurityConfiguration {
    /**
     * http security 是一个原型bean ...
     * 返回一个安全过滤链
     * @param httpSecurity security
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain actuatorSecurity(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
//                .requestMatcher(EndpointRequest.toAnyEndpoint())
                .authorizeHttpRequests()
                .anyRequest()
                .permitAll()
                .and()
                .build();
    }
}
