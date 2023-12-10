package org.example.spring.test.review.ioc;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.*;

/**
 * @author jasonj
 * @date 2023/11/3
 * @time 10:40
 * @description
 **/
public class AnnotationUtilsTests {

    @ContextConfiguration
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public  @interface  VV {

    }
    @VV
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited

    public @interface TT {

    }


    @ContextConfiguration
    public static class AnnotationTestsClass {

    }

    @VV
    public static class AnnotationTestsClass1 {

    }


    public static class AnnotationTestsClass2 extends AnnotationTestsClass1 {

    }

    @TT
    public static class AnnotationTestsClass3 {

    }

    @Test
    public void getAnnotationTest() {
        ContextConfiguration annotation = AnnotationUtils.getAnnotation(AnnotationTestsClass.class, ContextConfiguration.class);
        Assert.assertNotNull(annotation);

        ContextConfiguration annotation1 = AnnotationUtils.getAnnotation(AnnotationTestsClass1.class, ContextConfiguration.class);
        Assert.assertNotNull(annotation1);

        ContextConfiguration annotation2 = AnnotationUtils.getAnnotation(AnnotationTestsClass2.class, ContextConfiguration.class);
        Assert.assertNotNull(annotation2);


        // 只取最近的 .. 也就是距离类为0的注解或者,继承也是0的 TT 距离这个类的距离是2
        // 默认 get /find 都会存在Inherit 语义,但是对于tt  注解来说,它距离 类的距离是0, vv 注解是 1, contextConfiguration 同样是2
        // 如果这些注解在父类上,距离依旧不会变,因为是基于相对类的距离,所以 你能够发现AnnotationTestClass2 能够找到那个注解(因为 要求距离是0或者1)
        // 这就是getAnnotation的限制 ..
        ContextConfiguration annotation3 = AnnotationUtils.getAnnotation(AnnotationTestsClass3.class, ContextConfiguration.class);
        Assert.assertNotNull(annotation3);
    }
}
