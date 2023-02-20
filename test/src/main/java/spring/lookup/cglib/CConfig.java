package spring.lookup.cglib;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import spring.lookup.entity.Command;

/**
 * @author JASONJ
 * @dateTime: 2021-05-04 11:26:53
 * @description: cglib notice
 */
@Configuration
public class CConfig {
    /**
     * 这种情况下,永远不会被cglib代理,因为cglib只能代理非私有,final的且非静态的对象方法!
     * 意味着多次调用,会产生多个!
     * @return
     */
    @Bean(name = "command1")
    public static Command command(){

        Command command = new Command();
        command.setState(false);
        return command;
    }

    @Bean
    @Qualifier("command2")
    public Command command2(){
        Command command = new Command();
        command.setState(true);
        return command;
    }

    @Bean
    @Qualifier("command3")
    public Command command3(){
        return new Command();
    }
}
