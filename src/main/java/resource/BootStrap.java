package resource;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author JASONJ
 * @dateTime: 2021-05-17 16:03:09
 * @description: bootstrap
 */
public class BootStrap {
    // 基于xml的形式启动
      /*  public static void main(String[] args) throws IOException {
            ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext("classpath:/resource_resolve/applicationContext.xml");
            ResourceConfig bean = classPathXmlApplicationContext.getBean(ResourceConfig.class);
            try(InputStream inputStream = bean.getTemplate().getInputStream()){
                System.out.println(new String(inputStream.readAllBytes()));
        }
    }*/
    // 由于xml 和 注解驱动是混合一起测试,所以在基于注解驱动的时候,本身通过@Autowired注解的构造器会优先使用
    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(ResourceConfig.class);
        ResourceConfig bean = annotationConfigApplicationContext.getBean(ResourceConfig.class);
        try(InputStream inputStream = bean.getTemplate().getInputStream()) {
            System.out.println(new String(inputStream.readAllBytes()));
        }
    }
}
