package javaSe.clazz.annotation;

import java.lang.annotation.*;

@Repeatable(TestBases.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestBase{
    String value();
}
