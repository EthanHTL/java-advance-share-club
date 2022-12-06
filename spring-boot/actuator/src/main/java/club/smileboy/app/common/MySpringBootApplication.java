package club.smileboy.app.common;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.AliasFor;

@SpringBootApplication
public @interface MySpringBootApplication {

    @AliasFor(attribute = "excludeFilters",annotation = ComponentScan.class)
    ComponentScan.Filter[] excludeFilters() default {};
}
