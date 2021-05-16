package import_use.config;

import import_use.entity.AccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author JASONJ
 * @dateTime: 2021-05-15 14:48:16
 * @description: default repository
 */
@Configuration
public class DefaultRepositoryConfig implements RepositoryConfig{
    @Bean
    @Override
    public AccountRepository accountRepository() {
        return new AccountRepository();
    }
}
