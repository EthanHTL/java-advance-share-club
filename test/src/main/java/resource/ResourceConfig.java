package resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * @author JASONJ
 * @dateTime: 2021-05-17 15:58:33
 * @description: resource config
 *
 * 其中一种使用的是通过xml形式,
 * 当使用基于注解驱动的形式可以将资源路径作为系统属性!
 * 例如先使用@PropertySource 注入资源路径！
 */
@Component
@PropertySource("classpath:/resource_resolve/resource_path.properties")
public class ResourceConfig {
    private Resource template;
    public void setTemplate(Resource template) {
        this.template = template;
    }

    public Resource getTemplate(){
        return template;
    }

    public ResourceConfig(){

    }
    @Autowired
    public ResourceConfig(@Value("${myTemplate.path}") Resource resource){
        System.out.println("resource == "+resource);
        this.template = resource;
    }
}
