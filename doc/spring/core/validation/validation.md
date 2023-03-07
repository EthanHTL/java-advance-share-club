# 验证,数据绑定,类型转换

#### 概述
* 有时候在业务逻辑中考虑对属性进行验证有利有弊,spring 提供了一个对验证(以及数据绑定)提供了一种设计,但是没有排除它们其中的一个,特别是,验证不仅仅可以和web层结合使用并且更容易本地化, 并且它可以加载任意一个可用的验证器;
* <b>考虑这个疑虑,spring 提供了一个基本的Validator;在应用的每一层上都是有用的;
</b>
* 数据绑定是有用的(能够对用户动态绑定到领域模型中的输入进行验证,他不管如何处理用户输入的对象),spring提供了一个适合的名称的DataBinder,此类声在validation包中,它主要使用在web层但是不限于web层;
* BeanWrapper 在spring框架中是一个基本的概念并且被大量的使用;
然而你可能不会直接需要使用BeanWrapper,它可以用来绑定数据到对象上!
* spring的DataBinder以及低级别的BeanWrapper同时使用PropertyEditorSupport实现来解析并格式化属性值;PropertyEditor 以及PropertyEditorSupport 类型只是JavaBean规范的一部分并且将在此处进行阐述!
* Spring支持Java Bean验证(通过设置基础设施以及Spring自己的Validator相关的适配器),应用只需要一次启动Bean 验证(这一部分将在[ Java Bean Validation](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#validation-beanvalidation)
  使用它排除对所有验证的需要;在web层,引用能够更深层次的给Controller中每一个DataBinder注入Validator
  在[Configuring a DataBinder](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#validation-binder)
  进行设置,对于增强自定义验证逻辑非常有效!
#### 通过Validator 进行验证
通过Validator能够验证对象,此接口通过一个Errors对象工作,如果验证错误,验证器会报道失败到Errors中;
其次对于一个复杂对象上,可以封装每一个内嵌对象的验证，能够进行验证重用,例子可以查看当前文档的src包对应的validation文件夹下的例子;
验证的错误会通过验证器放入Errors中,在webMvc中,可以使用<b><spring:bind /> </b>标签检测错误消息,但是也可以自己检测Errors对象;
#### 解析错误消息的代码
错误代码和输出消息的联系,可以通过MessageSource输出错误消息,
在Errors上传递的参数,还会添加一些额外的参数,MessageCodesResolver会检测注入到Errors中的错误代码,默认会使用DefaultMessageCodesResolver,比如你使用rejectValue('age','too.darn.old')除了会注册too.darn.old代码之外,还会注册too.darn.old.age以及too.darn.old.age.int(这样做是为了方便开发人员在定位错误消息时提供帮助),MessageCodesResolver以及默认策略能够在MessageCodesResolver以及DefaultMessageCodesResolver中发现;
#### Bean 收集以及BeanWrapper
  BeanWrapper提供了一些功能去设置和获取属性值(分别或者统一),获取属性描述符,以及查询属性(如果它们是可读或者可写的);同时也提供对内嵌属性的支持,能够不限制深度的给子属性进行设置,也支持增加标准的PropertyChangeListeners和VetoableChangeListeners,不需要目标类支持这些代码,最后也提供通过index下标设置属性,这个BeanWrapper通常不会直接被应用代码使用(他可能会被DataBinder以及BeanFactory进行使用)
1) 内嵌使用[语法](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-beans-conventions)
#### 内建的PropertyEditor实现
它主要用于将String转换为实际的类型属性,它能够以不同的形式展示同一个对象的数据,如果需要有自定义的行为(需要注册自定义的编辑者,实现类 PropertyEditor),要么在BeanWrapper中注册定制的editor(编辑者),除此之外,要么放入Ioc容器中,PropertyEditor需要明确如何转换属性到指定的类型[ the javadoc of the java.beans package from Oracle.](https://docs.oracle.com/javase/8/docs/api/java/beans/package-summary.html)
* 完全通过PropertyEditor实现在bean上设置属性,当你在xml文件中使用string声明一些bean的value属性,如果设置了相关的Class参数(将使用ClassEditor去解析参数到具体的Class对象上);
* 在webMvc中解析http request parameter 通过各种各样的PropertyEditor实现(你也能够在CommandController的各种子类中进行绑定)
* spring 有大量的内建PropertyEditor实现,它们都在org.springframework.beans.propertyeditors中,大多数,但不是所有的都列在了下面的表中,默认情况下都注入到BeanWrapperImpl中,你同样可以注入自己的实现覆盖默认的,[propertyEditor](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-beans-conversion)
spring使用java.beans.PropertyEditorManager对可能需要的property editor设置查询路径,例如sun.bean.editors,他可能包括了Font,Color类型的实现,以及大多数基础类型;值得注意的是
  标准的JavaBean 基础设施将自动发现PropertyEditor(不需要显式注册),只需要将它放到需要处理的类的同包下,相同名称前缀后跟Editor名称即可;
```text
com
  chank
    pop
      Something
      SomethingEditor // the PropertyEditor for the Something class
```
当然也可以使用标准的BeanInfo javaBean 机制同样可以[具体](https://docs.oracle.com/javase/tutorial/javabeans/advanced/customization.html)
同样可以注册多个PropertyEditor实例,它们是和具体的类相联系的
```text
com
  chank
    pop
      Something
      SomethingBeanInfo // the BeanInfo for the Something class
```
beanInfo主要实现
```java
public class SomethingBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            final PropertyEditor numberPE = new CustomNumberEditor(Integer.class, true);
            PropertyDescriptor ageDescriptor = new PropertyDescriptor("age", Something.class) {
                @Override
                public PropertyEditor createPropertyEditor(Object bean) {
                    return numberPE;
                }
            };
            return new PropertyDescriptor[] { ageDescriptor };
        }
        catch (IntrospectionException ex) {
            throw new Error(ex.toString());
        }
    }
}
```
#### 注册额外的定制PropertyEditor
使用字符串设置bean属性,spring底层通过PropertyEditor转换字符串到复杂类型的属性,spring预先注册了大量的PropertyEditor实现(例如转换class字符串到Class对象),除此之外PropertyEditor查询机制:
允许适当地命名类的PropertyEditor，并将其与提供支持的类放在同一包中，以便可以自动找到它。
* 如果者需要注册一个自定义的PropertyEditor,可以使用各种各样的机制,但是使用ConfigurableBeanFactory的registerCustomEditor()不是很推荐,需要假设你有一个BeanFactory引用,除此之外使用bean factory 后置处理器,那么CustomEditorConfigurer可以完成这个任务,他是一个后置处理器,但是强烈推荐它和ApplicationContext使用,这样的话;它能够自动的被检测并注入到其他bean中;
* 所有的bean 工厂和应用上下文会自动的使用大量的内置属性编辑器(它们被BeanWrapper使用来处理属性转换),除此之外,ApplicationContext 也能够覆盖或者增加额外的编辑器去处理资源查找(在指定类型的应用上下文中使用合适的方式处理);

标准的JavaBean PropertyEditor实例就是为了转换属性值到实际类型,所以强烈建议使用CustomEditorConfigurer 而不是手动调用ConfigurableBeanFactory的registerCustomEditor()方法;

例如: 
```java
package example;

public class ExoticType {

    private String name;

    public ExoticType(String name) {
        this.name = name;
    }
}

public class DependsOnExoticType {

    private ExoticType type;

    public void setType(ExoticType type) {
        this.type = type;
    }
}
```
当属性设置完成了,如果想要将type属性设置为string,PropertyEditor 转换属性到实际的ExoticType 
```xml
<bean id="sample" class="example.DependsOnExoticType">
    <property name="type" value="aNameForExoticType"/>
</bean>
```
对应的转换器实现类似:
```java
package example;

public class ExoticTypeEditor extends PropertyEditorSupport {

    public void setAsText(String text) {
        setValue(new ExoticType(text.toUpperCase()));
    }
}
```
最终,使用CustomEditorConfigurer进行自定义属性编辑器注入
```xml
<bean class="org.springframework.beans.factory.config.CustomEditorConfigurer">
    <property name="customEditors">
        <map>
            <entry key="example.ExoticType" value="example.ExoticTypeEditor"/>
        </map>
    </property>
</bean>
```
#### 使用PropertyEditorRegistrar
另一种注册属性编辑器的方式就是通过Spring容器创建并使用一个PropertyEditorRegistrar,这个接口在几种不同的场景下你需要使用属性编辑器进行相同的设置,你能够写一个合适的注册器并且重用它;
PropertyEditorRegistrar 和PropertyEditorRegistry共同工作,其中BeanWrapper以及DataBinder实现了此接口（和PropertyEditorRegistry共同工作）,PropertyEditorRegistrar 在和CustomEditorConfigurer共同使用的时候非常方便[描述](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-beans-conversion-customeditor-registration),它暴露一个方法setPropertyEditorRegistrars(..)来方便的设置PropertyEditorRegistrar,并且可以非常容易和DataBinder、Controller共享;
因此,它可以避免在定义编辑器上的同步需求,因为一个PropertyEditorRegistrar 在每次尝试创建bean的时候都会创建一个新的PropertyEditor实例;
```java
package com.foo.editors.spring;

public final class CustomPropertyEditorRegistrar implements PropertyEditorRegistrar {

    public void registerCustomEditors(PropertyEditorRegistry registry) {

        // it is expected that new PropertyEditor instances are created
        registry.registerCustomEditor(ExoticType.class, new ExoticTypeEditor());

        // you could register as many custom property editors as are required here...
    }
}
```
对于其他示例可以查看org.springframework.beans.support.ResourceEditorRegistrar对于PropertyEditorRegistrar的实现,注意它对registerCustomEditors的实现,对于每个property editor都创建了一个新实例;<br/>
下面是一个例子,配置CustomEditorConfigurer并注册自定义的CustomPropertyEditorRegistrar;
```xml
<bean class="org.springframework.beans.factory.config.CustomEditorConfigurer">
    <property name="propertyEditorRegistrars">
        <list>
            <ref bean="customPropertyEditorRegistrar"/>
        </list>
    </property>
</bean>

<bean id="customPropertyEditorRegistrar"
    class="com.foo.editors.spring.CustomPropertyEditorRegistrar"/>
```
在使用SpringMvc框架时可以在回来看看这一部分描述,使用PropertyEditorRegistrar 和data-binding 的控制器(SimpleFormController)也是非常方便的;<br/>
下面展示了如何在initBinder方法中使用PropertyEditorRegistrar
```java
public final class RegisterUserController extends SimpleFormController {

    private final PropertyEditorRegistrar customPropertyEditorRegistrar;

    public RegisterUserController(PropertyEditorRegistrar propertyEditorRegistrar) {
        this.customPropertyEditorRegistrar = propertyEditorRegistrar;
    }

    protected void initBinder(HttpServletRequest request,
            ServletRequestDataBinder binder) throws Exception {
        this.customPropertyEditorRegistrar.registerCustomEditors(binder);
    }

    // other methods to do with registering a User
}
```
这种风格的PropertyEditor 注册能够简约代码(内聚)并且让普通PropertyEditor注册代码能够封装在类中并且如果有需要的话,它还能够在多个controller进行共享;

#### Spring Type Conversion
从spring3开始提供了通用类型转换系统,这个系统定义了一个SPI去实现转换逻辑以及一个Api去在运行时执行类型转换,在一个spring容器内部,你能够使用这个系统作为PropertyEditor的替代品(去转换扩展的bean属性字符串值到需要的类型属性上),你也能够使用公共Api(只要当你的应用中想要进行类型转换时);
##### Converter SPI
这是一个SPI,只需要实现它即可,它是一种强类型转换,为了创建你自己的转换器,需要实现以下接口;
```java
package org.springframework.core.convert.converter;

public interface Converter<S, T> {

    T convert(S source);
}
```
并且可以参数化类型S以及你想转换到目标类型的T(指定),你也能够透明的使用一种转换器(如果S是一个集合或者数组,然后将其转换为一个T类型的数组或者集合),只需要你提供这样的转换器并提供即可(DefaultConversionService已经这样做了);
* 每次调用convert(S)方法,S 默认必须不为空,你的Converter也许可以抛出一个未检查的异常,如果转换失败;  尤其是你应该抛出一个IllegalArgumentException报道一个无效的Source值,你需要关心的是确保你的Converter实现是线程安全的;
* 各种转换器实现已经在core.convert.support包默认提供了（为了方便）,包括了字符串到数字等其他的类型转换器;
##### 实现一个转换工厂 ConverterFactory
当你需要集中对某一个类层次的转换逻辑,例如String到Enum对象,那么可以实现ConverterFactory;
```java
package org.springframework.core.convert.converter;

public interface ConverterFactory<S, R> {

    <T extends R> Converter<S, T> getConverter(Class<T> targetType);
}
```
由于泛型变量已经定义了类的范围,那么你能够转换,实现getConverter(Class <T>)即可,这里T是R的子类,例如StringToEnumConverterFactory
```java
package org.springframework.core.convert.support;

final class StringToEnumConverterFactory implements ConverterFactory<String, Enum> {

    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverter(targetType);
    }

    private final class StringToEnumConverter<T extends Enum> implements Converter<String, T> {

        private Class<T> enumType;

        public StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        public T convert(String source) {
            return (T) Enum.valueOf(this.enumType, source.trim());
        }
    }
}
```
##### 使用GenericConverter
对于需要复杂的转换器实现,考虑使用GenericConverter接口,提供更多的灵活性但是缺少强类型约束,一个GenericConverter支持在多个资源和目标类型之间进行转换;除此之外,一个GenericConverter会提供必要的Source以及目标字段上下文(你能够使用它,当你实现了自己的转换逻辑),例如上下文使得类型转换通过字段的注解或者通过声明在字段签名上的通用信息进行驱动，下面展示了GenericConverter的接口定义:
```java
package org.springframework.core.convert.converter;

public interface GenericConverter {

    public Set<ConvertiblePair> getConvertibleTypes();

    Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType);
}
```
例如array -> collection 的转换,可以查看ArrayToCollectionConverter ,它检测声明了目标集合类型并解析了集合元素的类型,这样每一个array元素都能够转换到集合元素类型(在目标字段设置集合之前);<br/>
注意: 由于GenericConverter 是一个更加复杂的SPI接口,你应该在需要它的时候才使用,对于基础类型没必要使用它,因为Converter以及ConverterFactory已经足够了;

##### 使用ConditionalGenericConverter
有时,你可能想要一个Converter在条件成立的时候才进行调用,例如只有当指定的注解出现在某个目标字段上才进行执行,或者你只想在指定的方法调用时执行,ConditionalGenericConverter联合了ConditionalConverter和GenericConverter接口,让你定义自定义匹配条件(规范);
```java
public interface ConditionalConverter {

    boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType);
}

public interface ConditionalGenericConverter extends GenericConverter, ConditionalConverter {
}
```
一个好的例子是IdToEntityConverter(它在持久化实体标识符和实体引用之间进行转换),例如IdToEntityConverter可能仅仅在目标实体类型声明了一个静态的finder 方法时才进行执行(findAccount(Long)),你可能想要在执行此方法的时候进行检测,那么需要实现matches(TypeDescriptor, TypeDescriptor);
##### ConversionService 使用
使用它能够在运行时进行类型转换,转换器经常在以下接口背后运行,
```java
package org.springframework.core.convert;

public interface ConversionService {

    boolean canConvert(Class<?> sourceType, Class<?> targetType);

    <T> T convert(Object source, Class<T> targetType);

    boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType);

    Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType);
}
```
大多数的ConversionService都实现了ConverterRegistry,它对注册的Converter提供了SPI机制,在内部,ConversionService实现委托给注册的转换器进行类型转换逻辑处理;<br/>
一个健壮的ConversionService实现默认已经在core.convert.support包中提供了,
GenericConversionService 是一个通用目的实现能够合适的使用在大多数环境中,ConversionServiceFactory提供了一个转换工厂(能够创建公共的ConversionService配置);

##### 配置一个ConversionService
ConversionService 是一个无状态的对象设计在应用启动的时候实例化并且在多个线程中进行共享,在Spring应用中,你能够配置一个ConversionService实例(针对每一个应用上下文,或者Spring容器),spring本身会捆绑ConversionService并且在类型转换需要执行的时候通过框架使用它,你能够注入ConversionService到自己的Bean中然后直接的执行它;<br/>
如果没有给Spring注册ConversionService,那么会使用基于PropertyEditor的系统;
* 如何注册一个ConversionService
```xml
<bean id="conversionService"
    class="org.springframework.context.support.ConversionServiceFactoryBean"/>
```
默认的ConversionService能够转换各种各样的普通类型的数据(例如string,number,enums,collections,maps等等),为了使用或者覆盖默认的转换器(通过你自己的转换器实现),你可以设置converters属性,属性值可以是实现了Converter的任何类型,或者ConverterFactory、GenericConverter接口的类型;
```xml
<bean id="conversionService"
        class="org.springframework.context.support.ConversionServiceFactoryBean">
    <property name="converters">
        <set>
            <bean class="example.MyCustomConverter"/>
        </set>
    </property>
</bean>
```
在springMvc中也可以使用一个ConversionService,查看对应章节获取信息[Conversion and Formatting in the Spring MVC chapter](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-config-conversion) <br/>
在某些场景下,你也许希望在Conversion中使用Formatting,查看[ The FormatterRegistry SPI](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#format-FormatterRegistry-SPI) 使用FormattingConversionServiceFactoryBean的详细信息;

##### 编程式的使用ConversionService 
```java
@Service
public class MyService {

    public MyService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public void doIt() {
        this.conversionService.convert(...)
    }
}
```
大多数场景可以直接使用convert并指定目标类型,但是对于复杂类型它并不会工作,例如参数化元素的集合,举个例子:  List<Integer> -> List<String> 这样,他不会工作,如果需要转换,需要提供资源和目标类型的形式定义,因此可以使用TypeDescriptor提供各种各样的选项使得事情更加简单,如下所述:
```java
DefaultConversionService cs = new DefaultConversionService();

List<Integer> input = ...
cs.convert(input,
    TypeDescriptor.forObject(input), // List<Integer> type descriptor
    TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(String.class)));
```
注意DefaultConversionService自动给注入的转换器对于大多数环境来说足够了,包括了集合转换器,scalar 转换器,以及Object-> String的转换器,你能够向ConverterRegistry去注册相同的Converters(通过使用DefaultConversionService的静态方法addDefaultConverters ) <br/>
转换器能够重用array以及collections的value 类型,因此不需要创建一个特殊的转换器从Collection\<S> 到 Collection\<T>的转换,假设标准的集合处理已经是足够的;<br/>
#### Spring Field Formatting
前面说到ConversionService API非常健壮并且提供了一个Converter SPI实现类型转换,并且Spring能够使用此系统绑定Bean的属性值,除此之外,SPEL表达式以及DataBinder能够使用此系统绑定字段值,例如SPEL可以执行"expression.setValue(Object bean,Object value)"的表达式, core.convert管控系统执行;<br/>
如果在客户端环境中使用的时候,例如web 或者desktop应用中,可以需要将string转换为客户端后台运行程序识别的类型,同时也需要将其转换为string来支持视图渲染程序,除此之外,你可以经常需要本地化string数据,converter SPI不能直接说明解释格式化需求,所以spring3提供了Formatter SPI,它是另一中简单并健壮的PropertyEditor实现(对于客户端环境来说);<br/>
通常来说,你能够通过Converter SPI执行类型转换,但是不能够进行格式化,此时Formatter SPI能够支持这件事情,ConversionService提供了对两种SPI的类型转换API;
##### Formatter SPI
```java
package org.springframework.format;

public interface Formatter<T> extends Printer<T>, Parser<T> {
}
```
Formatter 继承了Printer 以及Parser 接口,
```java
public interface Printer<T> {

    String print(T fieldValue, Locale locale);
}
import java.text.ParseException;

public interface Parser<T> {

    T parse(String clientValue, Locale locale) throws ParseException;
}
```
只需要实现Formatter接口即可,print()方法用于实例的打印显示形式(在客户端的本地化显示),parse 从客户端本地化返回格式化的实例,Formatter应该抛出ParseException or an IllegalArgumentException(来表示解析失败的结果),需要注意Formatter实现应该线程安全;<br/>
format子包下包含了各种各样的Formatter实现,number包提供了各种跟数字相关的格式化器,
例如NumberStyleFormatter, CurrencyStyleFormatter, and PercentStyleFormatter 将Number对象通过java.text.NumberFormat进行格式化,datatime包提供了DateFormatter 将java.util.Date 通过java.text.DateFormat进行格式化; 
```java
package org.springframework.format.datetime;

public final class DateFormatter implements Formatter<Date> {

    private String pattern;

    public DateFormatter(String pattern) {
        this.pattern = pattern;
    }

    public String print(Date date, Locale locale) {
        if (date == null) {
            return "";
        }
        return getDateFormat(locale).format(date);
    }

    public Date parse(String formatted, Locale locale) throws ParseException {
        if (formatted.length() == 0) {
            return null;
        }
        return getDateFormat(locale).parse(formatted);
    }

    protected DateFormat getDateFormat(Locale locale) {
        DateFormat dateFormat = new SimpleDateFormat(this.pattern, locale);
        dateFormat.setLenient(false);
        return dateFormat;
    }
}
```
这是一个DateFormatter,spring欢迎社区贡献Formatter,[github Issues](https://github.com/spring-projects/spring-framework/issues)
<br/>
##### 基于注解驱动的Formatting
字段格式化能够通过字段类型或者注解进行配置,为了绑定注解到Formatter上,可以实现AnnotationFormatterFactory,以下展示了此接口的信息:
```java
package org.springframework.format;

public interface AnnotationFormatterFactory<A extends Annotation> {

    Set<Class<?>> getFieldTypes();

    Printer<?> getPrinter(A annotation, Class<?> fieldType);

    Parser<?> getParser(A annotation, Class<?> fieldType);
}
```
本质上通过它来对字段上标注了对应注解进行解析,例如DateTimeFormat,通过getFieldTypes()返回的类型集合判断此注解是否可用,通过getPrinter返回一个printer 返回此注解(对应格式化标识的注解,例如@NumberFormat)标注的字段的值,通过Parser解析成被注解注释的字段的值; \
例如 @NumberFormat注解的处理(能够处理数字风格或者模式):
```java
public final class NumberFormatAnnotationFormatterFactory
        implements AnnotationFormatterFactory<NumberFormat> {

    public Set<Class<?>> getFieldTypes() {
        return new HashSet<Class<?>>(asList(new Class<?>[] {
            Short.class, Integer.class, Long.class, Float.class,
            Double.class, BigDecimal.class, BigInteger.class }));
    }

    public Printer<Number> getPrinter(NumberFormat annotation, Class<?> fieldType) {
        return configureFormatterFrom(annotation, fieldType);
    }

    public Parser<Number> getParser(NumberFormat annotation, Class<?> fieldType) {
        return configureFormatterFrom(annotation, fieldType);
    }

    private Formatter<Number> configureFormatterFrom(NumberFormat annotation, Class<?> fieldType) {
        if (!annotation.pattern().isEmpty()) {
            return new NumberStyleFormatter(annotation.pattern());
        } else {
            Style style = annotation.style();
            if (style == Style.PERCENT) {
                return new PercentStyleFormatter();
            } else if (style == Style.CURRENCY) {
                return new CurrencyStyleFormatter();
            } else {
                return new NumberStyleFormatter();
            }
        }
    }
}
```
getFieldTypes 标志这NumberFormat注解用在那些类型上的字段才有效;
然后通过configureFormatterFrom为对应的printer以及parser返回formatter;
```java
public class MyModel {

    @NumberFormat(style=Style.CURRENCY)
    private BigDecimal decimal;
}
```
上述就是使用方式;
##### format annotation api
在org.springframework.format.annotation包中存放了许多格式化注解,例如:
```text
A portable format annotation API exists in the org.springframework.format.annotation package. You can use @NumberFormat to format Number fields such as Double and Long, and @DateTimeFormat to format java.util.Date, java.util.Calendar, Long (for millisecond timestamps) as well as JSR-310 java.time.
```
比如@DateTimeFormat 格式化java.util.Date 为ISO Date(yyyy-MM-dd)

##### FormatterRegistry api
它是一种SPI,主要用来注册formatter以及converters,FormattingConversionService是对FormatterRegistry的实现(对于大多数环境来说已经足够),你可以随意使用它,例如直接通过DataBinder进行使用,或者SPEL表达式使用,因为它已经使用了ConversionService;
```java
package org.springframework.format;

public interface FormatterRegistry extends ConverterRegistry {

    void addPrinter(Printer<?> printer);

    void addParser(Parser<?> parser);

    void addFormatter(Formatter<?> formatter);

    void addFormatterForFieldType(Class<?> fieldType, Formatter<?> formatter);

    void addFormatterForFieldType(Class<?> fieldType, Printer<?> printer, Parser<?> parser);

    void addFormatterForFieldAnnotation(AnnotationFormatterFactory<? extends Annotation> annotationFormatterFactory);
}
```
通过FormatterRegistry集中式配置,而不是通过controller中重复配置,例如:
你可能想要将所有的日期字段格式化(以某种方式,而不是在字段上放置指定的注解),通过一个共享的FormatterRegistry,你只需要定义一次规则,并且运用在任何地方上进行格式化;
##### FormatterRegistrar
FormatterRegistrar  是一个SPI(服务加载机制),只要是通过FormatterRegistry进行注册转换器和格式化器;
```java
package org.springframework.format;

public interface FormatterRegistrar {

    void registerFormatters(FormatterRegistry registry);
}
```
如果需要对一个已经给定的FormatterRegistry(这是一个格式化分类)注册多个converter或者formatter来说,这非常有效;
例如DateTimeFormatterRegistrar,DateFormatterRegistrar
如果相关的声明注册不足够,那么这种方式也能够满足;
当格式化程序需要在不同于其自身<T>的特定字段类型下进行索引时，或者在注册“printer/parser”键值对时,这也是有效的;

##### SpringMVC 中配置Formatting
查看[Conversion and Formatting](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-config-conversion)

##### 配置全局Date 和 Time Format
默认情况下,date将会转换为DateFormat.SHORT的形式,可以改写,但是需要确保Spring没有注册默认的formatters,手动注册
```text
org.springframework.format.datetime.standard.DateTimeFormatterRegistrar

org.springframework.format.datetime.DateFormatterRegistrar
```
例如:
```java
@Configuration
public class AppConfig {

    @Bean
    public FormattingConversionService conversionService() {

        // Use the DefaultFormattingConversionService but do not register defaults
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService(false);

        // Ensure @NumberFormat is still supported
        conversionService.addFormatterForFieldAnnotation(new NumberFormatAnnotationFormatterFactory());

        // Register JSR-310 date conversion with a specific global format
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setDateFormatter(DateTimeFormatter.ofPattern("yyyyMMdd"));
        registrar.registerFormatters(conversionService);

        // Register date conversion with a specific global format
        DateFormatterRegistrar registrar = new DateFormatterRegistrar();
        registrar.setFormatter(new DateFormatter("yyyyMMdd"));
        registrar.registerFormatters(conversionService);

        return conversionService;
    }
}
```
如果是xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd>

    <bean id="conversionService" class="org.springframework.format.support.FormattingConversionServiceFactoryBean">
        <property name="registerDefaultFormatters" value="false" />
        <property name="formatters">
            <set>
                <bean class="org.springframework.format.number.NumberFormatAnnotationFormatterFactory" />
            </set>
        </property>
        <property name="formatterRegistrars">
            <set>
                <bean class="org.springframework.format.datetime.standard.DateTimeFormatterRegistrar">
                    <property name="dateFormatter">
                        <bean class="org.springframework.format.datetime.standard.DateTimeFormatterFactoryBean">
                            <property name="pattern" value="yyyyMMdd"/>
                        </bean>
                    </property>
                </bean>
            </set>
        </property>
    </bean>
</beans>
```
值得注意的是使用的是FormattingConversionServiceFactoryBean; \
在Web程序中配置日期时间格式有其他考虑,参考:
[WebMVC Conversion and Formatting](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-config-conversion) 以及 [ WebFlux Conversion and Formatting](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-config-conversion)
