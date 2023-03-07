package club.smileboy.app.common.config;

import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class WebConfig {

    @Bean
    public AuditEventRepository auditEventRepository() {
        return new InMemoryAuditEventRepository();
    }

    /**
     * read://https_blog.csdn.net/?url=https%3A%2F%2Fblog.csdn.net%2Fweixin_41404773%2Farticle%2Fdetails%2F106355563
     * liquibase  数据库迁移 ...
     * @param dataSource
     * @return
     */
//    @Bean
//    public SpringLiquibase liquibase(DataSource dataSource) {
//        SpringLiquibase liquibase = new SpringLiquibase();
//        liquibase.setDataSource(dataSource);
//        //指定changelog的位置，这里使用的一个master文件引用其他文件的方式
//        liquibase.setChangeLog("classpath:liquibase/master.xml");
//        //liquibase.setContexts("development,test,production");
//        liquibase.setShouldRun(true);
//        return liquibase;
//    }

}
