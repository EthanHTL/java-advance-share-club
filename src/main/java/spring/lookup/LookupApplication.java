package spring.lookup;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import spring.lookup.config.ApplicationConfig;

import java.util.Scanner;

/**
 * @author JASONJ
 * @dateTime: 2021-04-29 23:31:03
 * @description: lookupApplication
 */
public class LookupApplication {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext();
        annotationConfigApplicationContext.register(ApplicationConfig.class);
        // 优雅的关闭回调
        annotationConfigApplicationContext.registerShutdownHook();
        annotationConfigApplicationContext.refresh();
        // 当调用上下文的start的方法时,通过lifeCycleListener的代理执行

        annotationConfigApplicationContext.start();
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        // 显式触发生命周期回调
        annotationConfigApplicationContext.close();

    }
}
