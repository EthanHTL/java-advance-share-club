package import_use.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author JASONJ
 * @dateTime: 2021-05-15 14:44:15
 * @description: base config
 */
@Configuration
@Import({ServiceConfig.class,DefaultRepositoryConfig.class})
public class BaseConfig {
}
