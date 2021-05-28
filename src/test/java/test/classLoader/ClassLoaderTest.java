package test.classLoader;

import org.junit.Test;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

/**
 * @author JASONJ
 * @dateTime: 2021-05-26 10:49:05
 * @description: classLoader test
 */
public class ClassLoaderTest {
    @Test
    public void test() throws IOException {
        // 从这里可以看出, 此类以当前类路径进行资源查找,并且如果不在maven中配置,包含在编码包中的资源无法包括在target目录中!
        // 所以资源加载,是从类路径上进行的!
        final InputStream resourceAsStream = ClassLoaderTest.class.getClassLoader().getResourceAsStream("./readme.txt");
        System.out.println(new String(resourceAsStream.readAllBytes()));
    }

    // 需要注意一点的是,getClass 会根据抓取当前对象的实际类型;
    @Test
    public void classLoad1(){
        // 加载 通过ClassUtils进行加载
        System.out.println(String[].class.getName()); // 获取名称 [内部形式名称]
        System.out.println(String[].class.getCanonicalName()); // 获取标准名称
        System.out.println(String[].class.getTypeName()); // 获取类型名称
        System.out.println(String[].class.getSimpleName()); // 获取简单名称
        System.out.println( Array.newInstance(String.class, 0).getClass()); // 通过反射实例出来的类对象
    }

    @Test
    public void test2() throws ClassNotFoundException {
        // 首先primitiveNamesType中抓取,如果没有,在从commonsCache中抓取!
        // 分为三种4中策略
        // 1.基本类型 直接查找,
        // 否则从普通类名中查询  如果格式是内部jls规范,也能直接查询到
        // 第三种  [] 结尾,人类识别的! [L开头,;结尾  [开头  然后进行处理!
        System.out.println(ClassUtils.forName("java.lang.Integer[]", null));
    }
}
