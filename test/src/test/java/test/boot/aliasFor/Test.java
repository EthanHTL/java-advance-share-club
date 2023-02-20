package test.boot.aliasFor;

import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;

/**
 * @author JASONJ
 * @dateTime: 2021-07-05 22:51:36
 * @description: test
 */
public class Test {
    public static void main(String[] args) {
        //TODO -- 查看spring 源码看他如何做
        XmlTestConfig xmlTestConfig = AnnotationUtils.synthesizeAnnotation(Application.class.getAnnotation(XmlTestConfig.class), Application.class);
        ContextConfiguration xmlTestConfig1 = AnnotationUtils.synthesizeAnnotation(XmlTestConfig.class.getAnnotation(ContextConfiguration.class), XmlTestConfig.class);
        System.out.println( xmlTestConfig1);
    }
}

