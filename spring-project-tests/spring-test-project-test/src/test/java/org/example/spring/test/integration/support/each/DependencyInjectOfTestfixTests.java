package org.example.spring.test.integration.support.each;

/**
 * 依赖注册的test fixture tests
 *
 * 依赖注册特性是一个默认支持的特性, DependencyInjectionTestExecutionListener默认配置来支持依赖注入 ..
 * 当使用JUnitJupiter 的时候可以优先选择构造器注入(查看SpringExtension)了解更多...
 * 当然基于字段或者setter注册也是可以的 ..
 *
 * 对于非JUnit Jupiter的测试框架，构造器注入没有任何效果，因为TCF不参与到测试类的实例化 ..
 *
 * 并且@Autowired 优先通过类型进行自动装配,.. 相关的注解的特性查看spring进行了解 ..
 *
 * 可以通过显式的配置TestExecutionListeners注解并从监听器列表中省略掉DependencyInjectionTestExecutionListener.class
 *
 * 因此依赖注入特性和spring框架本身提供的没有任何两样 ...
 */
public class DependencyInjectOfTestfixTests {



}
