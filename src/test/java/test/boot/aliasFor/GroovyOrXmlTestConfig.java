package test.boot.aliasFor;

import org.springframework.core.annotation.AliasFor;

/**
 * @author fanlianjie@ewell.cc
 * @createTime 2021/7/5 22:46
 * @description transitive implicit aliases with an annotation
 **/
@MyTestConfig
public @interface GroovyOrXmlTestConfig {
    @AliasFor(annotation = MyTestConfig.class,attribute = "groovyScripts")
    String[] groovy() default {};

    @AliasFor(annotation = ContextConfiguration.class,attribute = "locations")
    String[] xml() default {};
}
