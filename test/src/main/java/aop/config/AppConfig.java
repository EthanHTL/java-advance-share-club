package aop.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author JASONJ
 * @dateTime: 2021-05-30 08:41:05
 * @description:
 */
@Configuration
@ComponentScan("aop")
@EnableAspectJAutoProxy
public class AppConfig {

}
