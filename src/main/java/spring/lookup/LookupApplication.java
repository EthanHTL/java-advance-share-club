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
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(ApplicationConfig.class);
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
