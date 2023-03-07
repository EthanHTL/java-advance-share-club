#### Bean Validation
通过领域模型属性约束注解进行Bean 校验,例如:
```java
public class PersonForm {

    @NotNull
    @Size(max=64)
    private String name;

    @Min(0)
    private int age;
}
```
bean 验证器将对声明的约束进行验证,可以查看[Bean Validation](https://beanvalidation.org/)查看更多的Api,可以查看[Hibrenate 验证器](https://hibernate.org/validator/)学习如何设置bean 验证提供者(将其作为bean 配置);  

##### 配置一个Bean Validation Provider
spring提供了bean 验证的api支持(包括如何配置一个bean validation provider作为spring bean),首先可以拦截一个javax.validation ValidatorFactory 或者 javax.validation.Validator(在你需要进行验证的时候使用它); \
注意: 使用LocalValidatorFactoryBean配置一个默认的默认验证器;
```java
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class AppConfig {

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }
}
```
如果在类路径上存在对应的bean validation provider,那么能够自动被检测;

##### 拦截一个Validator
由于LocalValidatorFactoryBean实现了 javax.validation.ValidatorFactory 和 javax.validation.Validator,所以你也可以拦截org.springframework.validation.Validator,你可以任意的引用以上提到的接口的引用(他都会拿到一个验证器);
1) 拦截 javax.validation.Validator使用普通的验证API
2）拦截 org.springframework.validation.Validator 使用spring提供的bean 验证API
   
##### 配置自定义约束
每一个验证约束由两部分组成:
* 必须使用@Constraint 声明此约束并且他是一个可配置元素
* 要存在一个javax.validation.ConstraintValidator的实现并实现约束的行为; \
为了将声明和实现联系,每一个@Constraint 注解引用一个相关的ConstraintValidator实现类,在运行时,ConstraintValidatorFactory会尝试实例化引用的实现(当约束注解出现在领域模型中) \
默认来说,LocalValidatorFactoryBean配置了一个SpringConstraintValidatorFactory ,spring会创建ConstraintValidator实例,这能够让你更好的定制ConstraintValidator(和其他spring bean一样); \
声明一个注解约束:
```java
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=MyConstraintValidator.class)
public @interface MyConstraint {
}
```
约束验证器的实现
```java
import javax.validation.ConstraintValidator;

public class MyConstraintValidator implements ConstraintValidator {

    @Autowired;
    private Foo aDependency;

    // ...
}
```
##### spring 基于方法验证
通过MethodValidationPostProcessor bean集成方法验证特性到spring上下文中(通过 bean validation 1.1),并且还可以有自定义扩展(例如 hibernate validator 4.3)
```java
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@Configuration
public class AppConfig {

    @Bean
    public MethodValidationPostProcessor validationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}
```
为了启用spring-driven 方法验证,所有的目标类需要通过spring @Validated 注解,同时你也可以使用验证组,可以查看[MethodValidationPostProcessor](https://docs.spring.io/spring-framework/docs/5.3.7/javadoc-api/org/springframework/validation/beanvalidation/MethodValidationPostProcessor.html)来获取使用Hibernate 验证器以及 bean validation 1.1提供者对其进行详细配置的信息; \
由此可以看出,平时大多数使用的是基于方法驱动的验证机制; \
方法验证依赖于AOP 代理(代理了目标对象),jdk 动态代理或者CGLIB都是可以的,使用代理的限制可以查看[Understanding AOP Proxies](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#aop-understanding-aop-proxies \
注意: 除此之外需要记住只有在代理类上使用方法以及访问器才是有效的,直接对字段访问不会工作;

##### 可选的配置项
默认的LocalValidatorFactoryBean配置已经足够了,当然也有一些可选配置,例如使用消息插值来遍历解析,更多可以查看[ LocalValidatorFactoryBean](https://docs.spring.io/spring-framework/docs/5.3.7/javadoc-api/org/springframework/validation/beanvalidation/LocalValidatorFactoryBean.html);

##### 配置一个DataBinder
从spring3开始,能够给DataBinder配置一个验证器,一旦配置,你能够通过binder.validate()调用验证器,任意的验证错误将自动增加到binder对象的BindingResult;
```java
Foo target = new Foo();
DataBinder binder = new DataBinder(target);
binder.setValidator(new FooValidator());

// bind to the target object
binder.bind(propertyValues);

// validate the target object
binder.validate();

// get BindingResult that includes any validation errors
BindingResult results = binder.getBindingResult();
```
上述是一个验证过程; \
当然你可以配置多个Validator到一个DataBinder上,通过dataBinder.addValidators 以及 dataBinder.replaceValidators进行设置,在合并全局配置的bean 验证和spring Validator的局部配置到一个DataBinder实例是非常有效的,更多查看[spring mvc validation configuration](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-config-validation) \
##### spring mvc 3 validation
查看 [See Validation in the Spring MVC chapter.](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-config-validation)

