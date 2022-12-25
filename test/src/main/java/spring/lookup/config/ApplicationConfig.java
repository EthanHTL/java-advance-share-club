package spring.lookup.config;

import org.springframework.context.annotation.*;
import spring.lookup.autowired.TwoBean;
import spring.lookup.entity.Command;

/**
 * @author JASONJ
 * @dateTime: 2021-04-29 23:34:07
 * @description: config
 */
@Configuration
@ImportResource(locations = "classpath:applicationContext.xml")
@ComponentScan(basePackages = {"spring.lookup.autowired","spring.lookup.beanPostProcessor","spring.lookup.cglib"})
public class ApplicationConfig {

    @Bean
//    @Scope(scopeName = "prototype")
    public Command command(){
        Command command = new Command();
        command.setState("123");
        return command;
    }

    @Bean
    public TwoBean twoBean(){
        return new TwoBean();
    }

}
