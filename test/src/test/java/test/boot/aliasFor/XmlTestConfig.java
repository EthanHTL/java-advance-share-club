package test.boot.aliasFor;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author JASONJ
 * @dateTime: 2021-07-05 22:42:24
 * @description: explicit alias with an meta annotation
 */
@ContextConfiguration
@Target({ElementType.ANNOTATION_TYPE,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface XmlTestConfig {
    @AliasFor(annotation = ContextConfiguration.class,attribute = "locations")
    String[] xmlFiles();
}
