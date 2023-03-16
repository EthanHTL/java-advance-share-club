package aop;

import aop.config.AppConfig;
import aop.service.MyEhanceHandler.MyEhanceHandler;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.EnableLoadTimeWeaving;

import java.util.Scanner;

/**
 * @author JASONJ
 * @dateTime: 2021-05-30 08:40:51
 * @description: application
 */
@EnableLoadTimeWeaving
public class Application {
    public static void main(String[] args) {
        final AnnotationConfigApplicationContext configApplicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        configApplicationContext.getBean(MyEhanceHandler.class).enhance();
        final Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
