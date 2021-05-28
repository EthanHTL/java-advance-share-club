package test.spring.resource;

import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * @author JASONJ
 * @dateTime: 2021-05-27 14:14:24
 * @description: resource loader
 */
public class ResourceLoad {
    @Test
    public void test() throws IOException {
        final DefaultResourceLoader defaultResourceLoader = new DefaultResourceLoader();
        final Resource resource = defaultResourceLoader.getResource("classpath:config/");
        if(resource.exists()){

            final File[] files = resource.getFile().listFiles(File::isDirectory);
        }
    }
}
