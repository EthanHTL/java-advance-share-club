package test;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import java.util.Arrays;

/**
 * @author JASONJ
 * @dateTime: 2021-05-26 09:25:11
 * @description: receive
 */

public class PropertyReceive {

    public static void main(String[] args) {
        // 从这里我们可以看出  property 是一个Jvm参数
        System.out.println(System.getProperty("isjvm"));
        // 从这里我们可以看出 env 是一个环境变量属性
        System.out.println(System.getenv("isjjvm"));

        // spring中  根据环境抽象   jvm 优先级 > env
    }
}
