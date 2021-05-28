package test.spring.file;

import org.junit.Test;
import org.springframework.util.StringUtils;

/**
 * @author JASONJ
 * @dateTime: 2021-05-27 14:59:34
 * @description: test
 */
public class fileTest {

    /**
     * 格式化路径
     * 去除多余的./
     * 但是有些时候也需要 ./标识当前目录
     */
    @Test
    public void test(){
        System.out.println("结果"+StringUtils.cleanPath("classpath:./"));
    }
}
