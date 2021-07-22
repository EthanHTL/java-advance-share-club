package test.boot.aliasFor;

import org.springframework.core.annotation.AliasFor;

/**
 * @author JASONJ
 * @dateTime: 2021-07-05 22:44:11
 * @description: implicit alias with an annotation
 */
@ContextConfiguration
public @interface MyTestConfig {
     @AliasFor(annotation = ContextConfiguration.class, attribute = "locations")
     String[] value() default {};

     @AliasFor(annotation = ContextConfiguration.class, attribute = "locations")
     String[] groovyScripts() default {};

     @AliasFor(annotation = ContextConfiguration.class, attribute = "locations")
     String[] xmlFiles() default {};
}
