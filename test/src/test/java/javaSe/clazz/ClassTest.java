package javaSe.clazz;

import javaSe.clazz.annotation.TestBase;
import javaSe.clazz.annotation.TestBases;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

/**
 * @author JASONJ
 * @dateTime: 2021-05-27 18:18:50
 * @description: class test
 *
 * 主要用于java 类型反射相关的知识学习
 */
public class ClassTest {

    /**
     * 基础的类对象探索
     */
    @Test
    public void test1(){

        // 由此可以看出 Declaredxxx 可以获取所有字段 包括构造器
        for (Field declaredField : A.class.getDeclaredFields()) {
            System.out.println(declaredField.getName());
        }
        System.out.println("------------------------------------------------");
        // 由此可以看出 declared 是获取属于自己的所有字段
        for (Field declaredField : B.class.getDeclaredFields()) {
            System.out.println(declaredField.getName());
        }

        System.out.println("------------------------------------------------");
        // 由此看来可以获取公共字段
        for (Field field : B.class.getFields()) {
            System.out.println(field.getName());
        }
        System.out.println("------------------------------------------------");


    }

    // 普通方法
    @Test
    public void test2() throws ClassNotFoundException {

        System.out.println("--------------asSubclass-----------------------------");
        // 将该类对象转为A对象
        // 作为给定类型的一个子类型
        final Class<? extends A> aClass = B.class.asSubclass(A.class);

        System.out.println("--------------cast-----------------------------");
        //将一个对象强转为当前类对象的类型形式
        final B b = new B();
        final A cast = A.class.cast(b);
        System.out.println("--------------返回当前类的断言状态-----------------------------");
        System.out.println(A.class.desiredAssertionStatus());
        System.out.println("--------------AnnotatedType----------------------------------------------");
        //获取超类接口..... 不会翻译!
        System.out.println(Arrays.toString(FunctionalInterface.class.getAnnotatedInterfaces()));

        System.out.println(FunctionalInterface.class.getAnnotatedSuperclass());

    }


    @Test
    public void test3(){

        final Annotation[] annotations = A.class.getAnnotations();
        System.out.println(annotations == null);

        // 如果是容器注解中的子注解
        // 例如TestBase
        // 那么需要通过getAnnotationsByType(TestBase.class)拿取所有注解!

    }


    // 获取封闭的方法(方法中存在匿名内部类)
    // 当前类是否为一个匿名内部类
    // 如果是打印出包裹此类的方法
    @Test
    public void test(){
        new B().test();
    }

    // 如果是匿名内部类  打印出包裹它的类
    @Test
    public void test4(){
        new B().test1();
    }

    // 如果此类是匿名内部类,打印出它所在的构造器
    @Test
    public void test5(){
        new B("123");
    }

    // A 到底是不是B的父亲
    @Test
    public void test6(){
        System.out.println(A.class.isAssignableFrom(B.class));
    }

    // 给定的对象是否是当前类的实例
    @Test
    public void test7(){
        System.out.println(A.class.isInstance(new A()));

        System.out.println(B.class.isInstance(new A()));
    }

    // 此类是否为给定类的子类
    @Test
    public void test8(){
        System.out.println(B.class.asSubclass(A.class));

        // 将给此类转换为给定类的类型
//        System.out.println(A.class.asSubclass(B.class));
    }

    // 类型变量
    @Test
    public void test9(){
        class A<S> {

        }

        class B extends A<String>{

        }

        System.out.println(Arrays.toString(A.class.getTypeParameters()));
        final ParameterizedType genericSuperclass = (ParameterizedType) B.class.getGenericSuperclass();
    }

    @TestBases({@TestBase("百度一下"),@TestBase("小米")})
    static class A{
        private String name;
        String sex;
        protected String email;
        public String value;
    }
    static class B extends A{

        public B(){

        }
        public B(String value){
             class D{

            }

            System.out.println(D.class.getEnclosingConstructor());
        }
        private String age;
        public class C{

        }
        public void test(){
            class test{

            }
            // 包裹这个类的方法名称是
            System.out.println(test.class.getEnclosingMethod());
        }

        public void test1(){
            System.out.println(C.class.getEnclosingClass());
        }
    }


}
