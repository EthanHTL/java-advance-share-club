package resource.constructor;

import org.springframework.context.support.FileSystemXmlApplicationContext;
import resource.ResourceConfig;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author JASONJ
 * @dateTime: 2021-05-17 16:34:36
 * @description: file system application bootstrap
 */
public class FileSystemBootStrap {
    public static void main(String[] args) throws IOException {
        // 这里文件系统,相对于当前工作空间!
        // 相对路径是这样,如果使用绝对路径,那相对于操作系统的根目录!
        final FileSystemXmlApplicationContext fileSystemXmlApplicationContext = new FileSystemXmlApplicationContext("src/main/resources/resource_resolve/applicationContext.xml");
        final ResourceConfig bean = fileSystemXmlApplicationContext.getBean(ResourceConfig.class);
        try(final InputStream inputStream = bean.getTemplate().getInputStream()){
            System.out.println(new String(inputStream.readAllBytes()));
        }
    }
}
