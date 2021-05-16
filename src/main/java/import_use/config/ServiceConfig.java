package import_use.config;

import import_use.service.TransferService;
import import_use.service.impl.TransferServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author JASONJ
 * @dateTime: 2021-05-15 14:44:46
 * @description: service config
 */
@Configuration
public class ServiceConfig {
    @Autowired
    private RepositoryConfig repositoryConfig;

    @Bean
    public TransferService transferService() {
        return new TransferServiceImpl(repositoryConfig.accountRepository());
    }
}
