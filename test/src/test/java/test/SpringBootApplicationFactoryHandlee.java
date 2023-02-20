package test;

import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * @author JASONJ
 * @dateTime: 2021-05-26 09:48:35
 * @description: environment
 */
public class SpringBootApplicationFactoryHandlee {

    @Test
    public void factoryHandle() throws IOException {
        final Properties properties = PropertiesLoaderUtils.loadAllProperties("META-INF/spring.factories");
        System.out.println(properties);
        // 从这里可以看出springFactory的加载机制非常强势
    }
}
