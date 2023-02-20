package test.boot.aliasFor;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author JASONJ
 * @dateTime: 2021-07-05 22:41:02
 * @description: test
 * explicit alias with an annotation
 */
@Target({ElementType.ANNOTATION_TYPE,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ContextConfiguration {
    @AliasFor("locations")
    String[] value() default {};

    @AliasFor("value")
    String[] locations() default {};
}
