## 视图技术

视图技术在mvc应用中是限制在应用内部,视图能够访问应用上下文的所有bean,这就意味着不应该使用mvc的template支持(因为 此模板能够通过外部资源编辑),它存在安全隐患;

#### thymeleaf
thymeleaf 是一个服务端java渲染模板引擎,能够通过在浏览器端双击预览,也能够直接通过UI模板单独工作(例如设计者),不需要运行服务器,如果你想替代JSP,Thymeleaf提供了大多数扩展的特性来使得过渡更加容易; \
thymeleaf 和mvc集成由thymeleaf项目管理,只需要少量的配置就可以集成,例如ServletContextTemplateResolver,SpringTemplateEngine,ThymeleafViewResolver;

#### FreeMarker
此模板引擎能够输出各种文件内容(根据html),例如邮件内容,spring 内置freeMarker集成;
##### view配置
```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.freeMarker();
    }

    // Configure FreeMarker...

    @Bean
    public FreeMarkerConfigurer freeMarkerConfigurer() {
        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
        configurer.setTemplateLoaderPath("/WEB-INF/freemarker");
        return configurer;
    }
}
```
等价于
```xml
<mvc:annotation-driven/>

<mvc:view-resolvers>
    <mvc:freemarker/>
</mvc:view-resolvers>

<!-- Configure FreeMarker... -->
<mvc:freemarker-configurer>
    <mvc:template-loader-path location="/WEB-INF/freemarker"/>
</mvc:freemarker-configurer>
```
或者配置一个FreeMarkerConfigurer来完全控制
```xml
<bean id="freemarkerConfig" class="org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer">
    <property name="templateLoaderPath" value="/WEB-INF/freemarker/"/>
</bean>
```
你的模板需要存储在你在FreeMarkerConfigurer中配置的目录,会自动拼接前后缀,例如welcome,会寻找 /web-inf/freeMarker/welcome.ftl (这只是一个例子)
##### FreeMarker configuration
你能够通过FreeMarker的Settings属性以及SharedVariables直接配置Configuration对象(它将被Spring所管理),然后将它交给FreeMarkerConfigurer设置到合适的属性,freeMarkerSettings属性需要一个Properties对象,freemarkerVariables 属性需要一个java.util.map对象:
```xml
<bean id="freemarkerConfig" class="org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer">
    <property name="templateLoaderPath" value="/WEB-INF/freemarker/"/>
    <property name="freemarkerVariables">
        <map>
            <entry key="xml_escape" value-ref="fmXmlEscape"/>
        </map>
    </property>
</bean>

<bean id="fmXmlEscape" class="freemarker.template.utility.XmlEscape"/>
```
##### 表单处理
spring 提供了一个tag库能够在jsp中使用,也可以在其他视图技术中使用,<spring:bind/>元素,此元素主要让表单展示的值是表单背后的对象并且展示从验证器(在web或者业务层)验证失败的结果能够进行通过此表单进行响应,spring 也支持在freeMarker中使用相同的功能,增加一些可选的宏来生成一些自己的表单输入元素;
#### 绑定宏
宏的集合由spring-webmvc维护,所以必须导入依赖 \
有些宏定义在spring 模板库中仅仅考虑内部使用,但是宏定义中没有定义作用域范围,确保所有的宏对于用户模板以及方法调用是可见的,下面的一部分核心仅仅在于你需要在模板内进行直接宏调用才是必要的,如果你想在视图中直接进行宏代码查看,这个文件叫做spring.ftl并且放置在org.springframework.web.servlet.view.freeMarker包下;

##### 简单绑定
基于freeMarker的Html表单实际上作为一个表单视图传递给了controller,能够使用代码类似的绑定到字符value去展示错误信息(到每一个输入字段)-类似于jsp:
下面展示了一个personForm视图:
```ftl
<!-- FreeMarker macros have to be imported into a namespace.
    We strongly recommend sticking to 'spring'. -->
<#import "/spring.ftl" as spring/>
<html>
    ...
    <form action="" method="POST">
        Name:
        <@spring.bind "personForm.name"/>
        <input type="text"
            name="${spring.status.expression}"
            value="${spring.status.value?html}"/><br />
        <#list spring.status.errorMessages as error> <b>${error}</b> <br /> </#list>
        <br />
        ...
        <input type="submit" value="submit"/>
    </form>
    ...
</html>
```
<@Spring.bind>需要一个'path'参数,它由命令对象的名称组成(你可以自己设置控制器配置,例如设置命令对象名称)允许通过命令对象的字段名称结合”.“进行绑定，你也能够使用内嵌字段,例如command.address.street,bind宏假设默认的html 转义行为(通过ServletContext参数 defaultHtmlEscape进行指定,web.xml中) \
此外,<@spring.bindEscaped>形式能够接受两个参数显式指定html转义是否应该在状态错误消息或者数据中使用,你可以设置true/false进行处理,附加的表单处理宏简化了 HTML 转义的使用,你能该尽可能的使用宏,它们在下一部分解释:
##### input 宏
额外的方便的宏能够简化绑定和表单生成(包括错误信息展示),绝不需要使用这些宏去生成表单输入字段,能够混合并匹配它们(从简单html中或者直接调用spring bind 宏进行高亮选择) \
下面的表列出了freeMarker中能够使用的宏,以及参数列表:
* message(基于代码参数从资源束中获取字符串输出) <@spring.message code/>
* messageText(根据代码参数获取资源束对应的消息,如果失败使用默认代码参数) <@spring.messageText code,text/>
* url(根据应用根上下文作为前缀的相对路径) <@spring.url relativeUrl/>
* formInput (根据用户的输入收集标准的输入字段) <@spring.formInput path,attributes,fieldType/>
* formHiddenInput(表单的隐藏字段获取) <@spring.formHiddenInput path,attributes/>
* formPasswordInput(针对收集的密码的标准字段,如果没有值无法收集类型的字段) <@spring.formPasswordInput path,attributes/>
* formTextarea(获取 文本内容,自由形式的文本输入 ) <@spring.formTexarea path,attributes/>
* formSigngleSelect(下拉框选项,至少一个被选择) <@spring.formSingleSelect path,options,attributes/>
*formMultiSelect 多选收集 <@spring.formMultiSelect path,options,attributes/>
* formRadioButtons 单选收集 <@spring.formRadioButtions path,options separator,attributes/>
* formCheckboxes (获取checkboxes) <@spring.formCheckoutboxes path,options,separator,attributes/>
* formCheckbox 单个checkout <@spring.formCheckout path,attributes/>
* showErrors (获取绑定字段的验证错误) <@spring.showErrors separator,classOrStyle/> \
实际上 formHiddenInput,以及formPasswordInput实际上不需要,你能够使用普通formInput宏,然后指定hiden或者password作为fieldType参数的值即可生成一个符合的控件 \

参数详细信息:
* path 表示字段绑定的名称(例如  command.name)
* options 能够让input字段选择的必要属性,映射的键表示从表单返回并绑定到命令对象的值,map的key将作为label进行展示,也许不同于相关表单回传的相应值; 通常一个map会被controller使用作为一个参考数据(reference data),你也可以使用其他map实现,取决于需要的行为,对于严格存储的map,你能够使用TreeMap,等等; 
* separator 分割字符串序列
* attributes dom属性(字符串形式)
* classOrStyle 类名(针对于showErrors宏,以及使用span元素包装每一个错误信息),如果没有任何信息,那么错误通过<b>包装;
具体的小细节,这里不在叙述
##### 脚本视图
mvc支持react脚本视图
##### RSS 以及 atom
AbstractAtomFeedView 以及 abstractRssFeedView 继承至 AbstractFeedView 基类被用来提供ATOM 以及 RSS 视图,它们基于[ROME](https://rometools.github.io/rome/)项目,并定位在org.springframework.web.servlet.view.feed; \
AbstractAtomFeedView 需要你实现buildFeedEntries()方法以及可选的覆盖buildFeedMetaData() 方法(默认实现为空),例如:
```java
public class SampleContentAtomView extends AbstractAtomFeedView {

    @Override
    protected void buildFeedMetadata(Map<String, Object> model,
            Feed feed, HttpServletRequest request) {
        // implementation omitted
    }

    @Override
    protected List<Entry> buildFeedEntries(Map<String, Object> model,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // implementation omitted
    }
}
```
RSS 需要实现AbstractRssFeedView
```java
public class SampleContentRssView extends AbstractRssFeedView {

    @Override
    protected void buildFeedMetadata(Map<String, Object> model,
            Channel feed, HttpServletRequest request) {
        // implementation omitted
    }

    @Override
    protected List<Item> buildFeedItems(Map<String, Object> model,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // implementation omitted
    }
}
```
buildFeedItems() 以及 buildFeedEntries()方法在request中传递,能够访问locale,http 响应cooke的设置以及其他响应头的内容,feed 能够自动的在方法返回之后写入响应对象,
对于创建一个atom视图的例子,查看spring 团队博客条目[entry](https://spring.io/blog/2009/03/16/adding-an-atom-view-to-an-application-using-spring-s-rest-support)
#### PDF 以及 Excel
spring支持,引入文档视图,一个Html页面并不总是最好的用户视图,spring 也能够简单的生成pDF文档或者Excel 文档(根据model数据动态的生成),文档即视图并且从具有正确内容类型的服务器流式传输,希望启动客户端PC去运行它们的电子表格或者PDF视图的应用进行响应; \
为了使用excel视图 ,你需要增加APACHE POL库作为依赖,为了PDF生成,你需要增加一个OpenPDF库; \
强烈推荐OpenPDF(1.2.12)来替代 旧的 iText2.1.7; 
##### PDF视图
仅仅只需要实现org.springframework.web.servlet.view.document.AbstractPdfView然后实现buildDocument方法,例如:
```java
public class PdfWordList extends AbstractPdfView {

    protected void buildPdfDocument(Map<String, Object> model, Document doc, PdfWriter writer,
            HttpServletRequest request, HttpServletResponse response) throws Exception {

        List<String> words = (List<String>) model.get("wordList");
        for (String word : words) {
            doc.add(new Paragraph(word));
        }
    }
}
```
一个控制器能够返回这样的一个视图而不是从其他扩展视图定义(通过名称引用)或者从handler方法中返回一个View实例;
##### Excel 视图
spirng 4.2开始 org.springframework.web.servlet.view.document.AbstractXlsView 提供作为excel视图的基类,基于apache pol,有特殊的AbstractXlsxView或者AbstractXlsxStreamingView) 取代过时的AbstractExcelView 类; \
程序模型类似于AbstractPdfView,但是buildExcelDocument是核心模板方法并且控制有能力返回这样的一个视图(从外部定义-通过名称)或者从handler方法中返回一个View实例;

#### Jackson
spring 提供jackson 库支持
##### 基于jackson 的 json mvc 视图
MappingJackson2JsonView 基于ObjectMapper 去渲染响应内容作为JSON,默认情况模型的内容将编码为JSON(除了框架指定部分类之外),这种情况下map的内容需要过滤,你能够指定一个模型属性的集合-需要编码的(通过使用modelKeys属性),捏能够使用extractValueFromSingleKeyModel属性包含单值(model中的单个key)抓取并序列化而不是作为模型属性的映射; \
你能够定制JSON 映射(通过jackson提供的注解),当你需要完全控制时,你能够注入一个ObjectMapper到objectMapper属性上,完全自定义;
##### jackson-based xml 视图
MappingJackson2XmlView 使用 jackson xml 扩展 XmlMapper 渲染xml的相应内容,如果model中包含了多个条目,你应该显式设置需要序列化的对象-modelKey属性设置,如果model包含了单个条目,自动序列化; \
你能够定制xml映射(通过使用jaxb 或者 jackson提供的注解),如果你需要更深层次的控制,注册一个自定义的xmlMapper到ObjectMapper属性中,提供自己的序列化和反序列化器;
##### xml Marshalling
MarshallingView 使用一个Xml Marshaller (定义在org.springframework.oxm)去渲染响应内容作为xml,你能够显式设置需要marshalled的对象,使用modelKey指定,除此之外，此视图迭代所有的model属性以及marshals 第一个类型(被Marshaller所支持),获取更多信息和函数查看org.springframework.oxm包,查看[Marshalling xml using o/x mappers](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/data-access.html#oxm)
#### XSLT Views
XSLT 是 XML 的一种转换语言，作为 Web 应用程序中的视图技术很受欢迎。如果您的应用程序自然地处理 XML 或者您的模型可以轻松地转换为 XML，那么 XSLT 作为视图技术是一个不错的选择。以下部分展示了如何将 XML 文档生成为模型数据，并在 Spring Web MVC 应用程序中使用 XSLT 对其进行转换 \
这个例子是一个简单的 Spring 应用程序，它在控制器中创建一个单词列表并将它们添加到模型映射中。返回model map以及 XSLT 视图的视图名称。有关 Spring Web MVC 的控制器接口的详细信息，请参阅带注释的控制器。 XSLT 控制器将单词列表转换为准备转换的简单 XML 文档 \
##### bean
配置XsltViewResolver bean以及普通的mvc注解配置
```java
@EnableWebMvc
@ComponentScan
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public XsltViewResolver xsltViewResolver() {
        XsltViewResolver viewResolver = new XsltViewResolver();
        viewResolver.setPrefix("/WEB-INF/xsl/");
        viewResolver.setSuffix(".xslt");
        return viewResolver;
    }
}
```
##### 控制器内容
```java
@Controller
public class XsltController {

    @RequestMapping("/")
    public String home(Model model) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = document.createElement("wordList");

        List<String> words = Arrays.asList("Hello", "Spring", "Framework");
        for (String word : words) {
            Element wordNode = document.createElement("word");
            Text textNode = document.createTextNode(word);
            wordNode.appendChild(textNode);
            root.appendChild(wordNode);
        }

        model.addAttribute("wordList", root);
        return "home";
    }
}
```
目前为止,我们仅仅创建了一个Dom文档并增加到model中,能够加载一个xml文件作为Resource并使用它替代自定义Dom文档; \
有可用的软件包可以自动“控制”对象图，但是，在 Spring 中，您可以完全灵活地以您选择的任何方式从模型创建 DOM。这可以防止 XML 的转换在模型数据的结构中扮演过大的角色，这在使用工具管理 DOM 化过程时是一种危险 \
##### Transformation
最终XsltViewResolver 解析 "home"XSLT 文件并且合并DOM文档生成我们的视图,xslt模板展示在war文件 /web-inf/xsl目录下并且存在xslt文件扩展
```xslt
<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="html" omit-xml-declaration="yes"/>

    <xsl:template match="/">
        <html>
            <head><title>Hello!</title></head>
            <body>
                <h1>My First Words</h1>
                <ul>
                    <xsl:apply-templates/>
                </ul>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="word">
        <li><xsl:value-of select="."/></li>
    </xsl:template>

</xsl:stylesheet>
```
最后产生的Html
```html
<html>
    <head>
        <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Hello!</title>
    </head>
    <body>
        <h1>My First Words</h1>
        <ul>
            <li>Hello</li>
            <li>Spring</li>
            <li>Framework</li>
        </ul>
    </body>
</html>
```