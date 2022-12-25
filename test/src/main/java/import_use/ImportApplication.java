package import_use;

import import_use.config.BaseConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Scanner;

/**
 * @author JASONJ
 * @dateTime: 2021-05-15 14:43:22
 * @description: import application
 */
public class ImportApplication {
    public static void main(String[] args){
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(BaseConfig.class);
        Scanner scanner = new Scanner(System.in);
        System.out.println(scanner.nextLine());
    }
}
