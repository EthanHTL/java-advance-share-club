package test.boot;

import org.junit.Test;
import org.springframework.boot.env.RandomValuePropertySource;

/**
 * @author JASONJ
 * @dateTime: 2021-05-27 10:05:49
 * @description: test
 */
public class RandomPropertySourceTest {
    @Test
    public void test(){
        // 随机数资源 PropertySource
        final RandomValuePropertySource random1 = new RandomValuePropertySource("random1");
//        System.out.println(random1.getProperty("random.123123"));

        System.out.println(random1.getProperty("random.int(3,6)"));
    }
}
