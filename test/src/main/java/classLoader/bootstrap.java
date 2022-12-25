package classLoader;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * @author JASONJ
 * @dateTime: 2021-05-17 18:03:09
 * @description: bootstrap
 */
public class bootstrap {
    public static void main(String[] args) throws IOException {
        // 通过空格可以返回当前可能需要搜索的上下文根目录
        System.out.println(ClassLoader.getSystemResource(""));
        System.out.println(URLClassLoader.getSystemResource(""));

        test1();
    }

    // 根据官网对classpath和 ant-style pattern模式的使用解释
    // 如果一个基本包下存在多个类路径,有可能无法查询到所需要的资源
    public static void test1() throws IOException {
        // 根据ant-style 匹配说明,如果没有以至少一个根目录开头,可能会导致无法找到资源
        // 但是测试没有效果!
        System.out.println(new ClassPathXmlApplicationContext("classpathload/**/applicationContext.xml"));
//        System.out.println(new ClassPathXmlApplicationContext("/classpathload/bootstrap/**/applicationContext.xml"));
    }
}
