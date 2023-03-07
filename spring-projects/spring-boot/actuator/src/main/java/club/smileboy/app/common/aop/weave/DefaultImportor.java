package club.smileboy.app.common.aop.weave;

import club.smileboy.app.common.aop.weave.bean.EntitlementCalculationService;
import org.springframework.context.annotation.Bean;

public class DefaultImportor  {

    @Bean
    public EntitlementCalculationService service() {
        return new EntitlementCalculationService();
    }
}
