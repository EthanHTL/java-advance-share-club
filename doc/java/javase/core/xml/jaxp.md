# jaxp 1.5 以及新的属性

## 背景

JAXP 安全处理功能对 XML 处理器设置了资源限制，以应对某些类型的拒绝服务攻击。但是，它不限制获取外部资源的方式，这在尝试安全地处理 XML 文档时也很有用。
当前的 JAXP 实现支持可用于实施此类限制的特定于实现的属性，但是需要一种标准的方法来执行此操作。

JAXP 1.5 添加了三个新属性及其相应的系统属性，以允许用户指定允许或不允许的外部连接类型。属性值是协议列表。 
JAXP 处理器通过将协议与列表中的协议进行匹配来检查是否允许给定的外部连接。如果连接在列表中，处理器将尝试建立连接，否则拒绝连接。
JAXP 1.5 已集成到 7u40 和 JDK8 中。

## 外部资源

XML、Schema 和 XSLT 标准支持以下需要外部资源的结构。 JDK XML 处理器的默认行为是建立连接并获取指定的外部资源。

- 外部DTD: 引用一个外部的文档类型定义(DTD),例如 <!DOCTYPE root_element SYSTEM "url">
- 外部实体引用: 涉及外部数据,语法: <!ENTITY name SYSTEM "url">

    常见实体引用如下:

```xml
<?xml version="1.0" standalone="no" ?>
<!DOCTYPE doc [<!ENTITY otherFile SYSTEM "otherFile.xml">]>
<doc>
    <foo>
    <bar>&otherFile;</bar>
    </foo>
</doc>
```

- 外部参数实体,语法 <!ENTITY % name SYSTEM url>,例如:
```xml
<?xml version="1.0" standalone="no"?>
    <!DOCTYPE doc [
      <!ENTITY % foo SYSTEM "http://www.example.com/student.dtd"<
      %foo;
    ]>
```

- XInclude: 包括一个外部信息集到xml 文档中
- 引用 XML schema 组件 - 使用schemaLocation属性,import,include 元素 ..
```text
schemaLocation="http://www.example.com/schema/bar.xsd"
```

- 使用import / include 元素合并样式表,语法: <xsl: include href="include.xsl" />
- xml 样式表处理指令: 被用来包括样式表在xml文档中,语法: <?xml-stylesheet href="foo.xsl" type="text/xsl" ?>
- xslt document()函数: 被用来访问外部xml文档的节点, 例如 <xsl:variable name="dummy" select="document('DocumentFunc2.xml') />


## 新属性

jaxp 1.5 定义了下面三个新属性能够被用来调节 是否xml 处理器能够解析上面列出的外部资源 .. 这个属性是:
- javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD
- javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA
- javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET


这些api 属性有相应的系统属性和jaxp.properties
### ACCESS_EXTERNAL_DTD
Name: http://javax.xml.XMLConstants/property/accessExternalDTD
Definition: 限制对指定协议的外部 DTD、外部实体引用的访问。
Value: see Values of the Properties
Default value: all, connection permitted to all protocols.
System property: javax.xml.accessExternalDTD

### ACCESS_EXTERNAL_SCHEMA
Name: http://javax.xml.XMLConstants/property/accessExternalSchema
Definition: 限制对由 schemaLocation 属性、Import 和 Include 元素设置的外部引用指定的协议的访问。
Value: see Values of the Properties
Default value: all, connection permitted to all protocols.
System property: javax.xml.accessExternalSchema

### ACCESS_EXTERNAL_STYLESHEET
Name: http://javax.xml.XMLConstants/property/accessExternalStylesheet
Definition: 限制访问STYLESHEET处理指令，文档功能，导入和Incluble Element 设置的外部参考去指定的协议的访问。
Value: see Values of the Properties
Default value: all, connection permitted to all protocols.
System property: javax.xml.accessExternalStylesheet

### ${java.home}/lib/jaxp.properties
这些属性能够在jaxp.properties中发现(他们为使用java运行时的所有应用定义了行为),形式是(property-name=[value],[,value]*),例如:

javax.xml.accessExternalDTD=file,http

这些属性名等同于这些属性: javax.xml.accessExternalDTD,javax.xml.accessExternalSchema 以及 javax.xml.accessExternalStylesheet ..

### 这些属性的值

所有属性的值都具有相同格式.. 

Value:  协议通过逗号分隔, 一个协议可以是uri的scheme 部分 或者JAR协议的情况下,"jar" + 由分号分隔的scheme 不分.. 一个scheme的定义如下:
schema = alpha * (alpha | digit | "+" | "-" | ".")

这里的alpha = a-z 以及 A-Z

以及 JAR协议:
jar[:scheme] ..


协议是大小写不敏感的,在value中由Character.isSpaceChar所定义的任何空格都将被忽略 .. 一个协议实例是: file ,http,jar:file

默认值: 默认值是特定于实现, 在JAXP1.5 RI, java SE 7u40,以及 javase 8 ,默认值 都是all, 授予所有协议 ..

granting all access: all 表示全部允许,例如 javax.xml.accessExternalDTD=all(在jaxp.properties中)允许系统在访问外部DTD 以及 实体引用上没有任何限制 ..

denying any access: 一个空的字符串即可, 那么就是拒绝所有协议 .. 例如 javax.xml.accessExternalDTD="" 那么表示拒绝jaxp 处理器去处理任何外部连接 ..

### scope 以及 order
javax.xml.XMLConstants#FEATURE_SECURE_PROCESSING (FSP) 是 XML 处理器（包括 DOM、SAX、模式验证、XSLT 和 XPath）的必需功能。
当设置为 true 时，建议实现启用由上面指定的新属性定义的访问限制。为了兼容性，JAXP 1.5 不启用新限制，尽管 FSP 对于 DOM、SAX 和 Schema Validation 默认为 true。

对于 JDK 8，建议在显式设置 FSP 时将新的 accessExternal* 属性设置为空字符串。只有通过 API 设置 FSP 时才会出现这种情况，例如 factory.setFeature(FSP, true)。
尽管 FSP 对于 DOM、SAX 和模式验证在默认情况下为真，但它并未被视为“显式”设置，因此 JDK 8 默认情况下不设置限制。


jaxp.properties 文件中指定的属性会影响 JDK 或 JRE 的所有调用，并将覆盖它们的默认值或可能已由 FEATURE_SECURE_PROCESSING 设置的值。

设置系统属性后，将仅影响一次调用，并将覆盖默认设置或 jaxp.properties 中设置的设置，或可能已由 FEATURE_SECURE_PROCESSING 设置的设置。

通过 JAXP 工厂或 SAXParser 指定的 JAXP 属性优先于系统属性、jaxp.properties 文件以及 javax.xml.XMLConstants#FEATURE_SECURE_PROCESSING。

在以下情况下，新的 JAXP 属性对它们试图限制的相关构造没有影响：

- 当存在解析器且解析器返回的源不为空时(也就是可能使用了自定义方式来解析)。这适用于可能在 SAX 和 DOM 解析器上设置的实体解析器、StAX 解析器上的 XML 解析器、SchemaFactory 上的 LSResourceResolver、Validator 或 ValidatorHandler 或转换器上的 URIResolver。
- 当通过调用 SchemaFactory 的 newSchema 方法显式创建模式时。
- 当不需要外部资源时。例如，参考实现支持以下特性/属性，可用于指示处理器不加载外部 DTD 或解析外部实体。
  ```text
    http://apache.org/xml/features/disallow-doctype-decl true
    http://apache.org/xml/features/nonvalidating/load-external-dtd false
    http://xml.org/sax/features/external-general-entities false
    http://xml.org/sax/features/external-parameter-entities false
  ```
  
### 和SecurityManager 的 关系
jaxp 属性将会首先检查 - 在一个连接尝试之前 - 无论SecurityManager 出现. 这意味着一个连接也许会被阻塞 - 即使它被SecurityManager 授权 .. 例如:
如果jaxp属性被设置去禁用http 协议,它们将有效的禁用任何连接尝试 - 即使当应用也具有SocketPermission ..

为了限制连接的目的,SecurityManager 能够视为一个更底层的事物, 权限将会在JAXP 属性评估之后进行检查, 如果一个应用没有 SocketPermission 权限, 那么SecurityException 
将会抛出 - 即使 JAXP 属性设置允许进行http 连接 ..

当 SecurityManager 存在时，JAXP FEATURE_SECURE_PROCESSING 设置为 true。此行为不会启用新的限制。

### 在jdk中的属性设置
以下的表展示了 在Jdk中设置的新属性的默认值和行为 ..

| 访问属性的值 | 默认值 | set FSP(a) | jaxp.properties | system property | api property |
|--- |--- |--- |--- |--- |--- |
| 7u40 | a11 | no change | override | override | override |
|jdk8| a11 | change to "" | override | override | override |

a. 设置fsp 意味着设置 FEATURE_SECURE_PROCESSING 通过使用JAXP 工厂的setFeature 方法来显式调用 ..

b.他们之间仅仅行为不同 - 在 7u40 以及 jdk8之间 - 设置fsp 将不需要改变 accessExternal* 属性 - 在 7u40中, 但是在 jdk8中需要设置fsp为 空属性, jdk8中设置 fsp 是一个可选的 ..

### 使用属性
#### 什么时候使用这些属性

仅当应用程序处理不受信任的 XML 内容时，才需要限制获取外部资源。不处理不受信任内容的内部系统和应用程序无需关注新限制或进行任何更改。由于 7u40 和 JDK8 默认没有此类限制要求，因此应用程序在升级到 7u40 和 JDK8 时不会经历任何行为变化。
对于确实处理不受信任的 XML 输入、模式或样式表的应用程序，如果已经存在安全措施，例如启用 Java 安全管理器以仅授予受信任的外部连接，或使用解析器解析实体，则不需要新功能在 JAXP 1.5 中添加。
但是，JAXP 1.5 确实为在没有安全管理器的情况下运行的系统和应用程序提供了直接的保护。对于此类应用程序，可以通过使用下面详细描述的新功能来考虑限制。

#### 通过api 设置属性

当更改代码可行时，通过 JAXP 工厂或解析器设置新属性是启用限制的最佳方式。可以通过以下接口设置属性：
```java
DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
dbf.setAttribute(name, value);
 
SAXParserFactory spf = SAXParserFactory.newInstance();
SAXParser parser = spf.newSAXParser();
parser.setProperty(name, value);
 
XMLInputFactory xif = XMLInputFactory.newInstance();
xif.setProperty(name, value);
 
SchemaFactory schemaFactory = SchemaFactory.newInstance(schemaLanguage);
schemaFactory.setProperty(name, value);
 
TransformerFactory factory = TransformerFactory.newInstance();
factory.setAttribute(name, value);
```
The following is an example of limiting a DOM parser to local connection only for external DTDs:
```java
DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
try {
dbf.setAttribute({{XMLConstants.ACCESS_EXTERNAL_DTD}}, "file, jar:file");
} catch (IllegalArgumentException e) {
//jaxp 1.5 feature not supported
}
```
如果可以更改代码，并且对于新开发，建议按照上面的说明设置新属性。通过以这种方式设置属性，无论应用程序部署到较旧或较新版本的 JDK，还是通过系统属性或 jaxp.properties 设置属性，应用程序都可以确保保持所需的行为。

#### 使用系统属性
```java
//allow resolution of external schemas

System.setProperty("javax.xml.accessExternalSchema", "file, http");

//this setting will affect all processing after it's set
some processing here

//after it's done, clear the property
System.clearProperty("javax.xml.accessExternalSchema");
```


#### 使用jaxp.properties
jaxp.properties 是一个简单的配置文件, 它位于 $(java.home)lib/jaxp.properties java.home 就是jre的安装路径 ..

一个外部访问限制可以通过增加以下行到jaxp.properties 文件中 ..
```java
javax.xml.accessExternalStylesheet=file, http
```
当这个设置的时候,所有的jdk / jre 的调用将会注意针对外部样式表的加载限制 ..

这个特性也许有用 - 如果系统不想要允许外部连接(通过xml 处理器),在这种情况下,这三个属性可以设置来进行限制(例如仅仅本地文件) ..


## 错误处理
由于这些属性是当前版本的新属性，因此建议应用程序捕获接口特有的异常，例如以下示例中的 SAXException。捕获应用程序可能在旧版本上正常工作，例如示例代码包含以下方法，用于检测示例是否使用支持新属性的 JDK 或 JAXP 实现版本运行：
```java
public boolean isNewPropertySupported() {
       try {
           SAXParserFactory spf = SAXParserFactory.newInstance();
           SAXParser parser = spf.newSAXParser();
           parser.setProperty("http://javax.xml.XMLConstants/property/accessExternalDTD", "file");
       } catch (ParserConfigurationException ex) {
           fail(ex.getMessage());
       } catch (SAXException ex) {
           String err = ex.getMessage();
           if (err.indexOf("Property 'http://javax.xml.XMLConstants/property/accessExternalDTD' is not recognized.") > -1)
           {
             //expected, jaxp 1.5 not supported
             return false;
           }
       }
       return true;
  }

```

如果由于新属性设置的限制而拒绝访问外部资源，将抛出以下格式的错误异常：

```text
[type of construct]: Failed to read [type of construct] "[name of the external resource]", because "[type of restriction]" access is not allowed due to restriction set by the [property name] property.
```
例如，如果由于对 http 协议的限制而拒绝获取外部 DTD，如下所示：
```text
parser.setProperty("http://javax.xml.XMLConstants/property/accessExternalDTD", "file");
```
如果解析器解析包含对“http://java.sun.com/dtd/properties.dtd”的外部引用的 XML 文件，错误消息将如下所示：
```text
External DTD: Failed to read external DTD ''http://java.sun.com/dtd/properties.dtd'', because ''http'' access is not allowed due to restriction set by the accessExternalDTD property.
```

## StAX
对于StAX的规范,jsr 173, 然而没有支持新属性, StAX在JAXP的内容中 包括了对这些属性支持, 设置这些属性类似于sax / dom,但是通过XMLInputFactory 设置如下:
```java
XMLInputFactory xif = XMLInputFactory.newInstance();
xif.setProperty("http://javax.xml.XMLConstants/property/accessExternalDTD", "file");
```
这些存在的属性和特性在StAX中指定, jsr 173 规范相比这些新的属性具有更高的优先级 .. 例如 SupportDTD 属性, 当设置为false, 将导致程序抛出异常(例如当输入文件包含了 dtd 并在解析之前抛出异常),
对于禁用了dtd的所有应用来说, 新属性的增加不会影响它们 ...

## 总结
JAXP 1.5 提供了新的属性来控制获取外部资源给用户。新属性的使用与其他现有属性相同，不同之处在于为属性提供了相应的系统属性和 jaxp.properties，以便它们可用于系统范围的限制或权限。

## 参考

- [jsr 206jaxp 1.55 维护](http://www.jcp.org/en/jsr/detail?id=206)
- [7u40 发行注意事项](http://www.oracle.com/technetwork/java/javase/7u40-relnotes-2004172.html)
- [jdk8 api 以及 document](https://docs.oracle.com/javase/8/docs/index.html)
- [jdk 7 api 以及 document](https://docs.oracle.com/javase/7/docs/index.html)
- [jep 1855](http://openjdk.java.net/jeps/185)

## 属性限制
XML 处理有时可能是内存密集型操作。应用程序，尤其是那些接受来自不受信任来源的 XML、XSD 和 XSL 的应用程序，应该采取措施通过使用 JDK 中提供的 JAXP 处理限制来防止内存消耗过多。
开发人员应评估其应用程序的要求和操作环境，以确定其系统配置的可接受限制并相应地设置这些限制。与大小相关的限制可用于防止在不消耗大量内存的情况下处理格式错误的 XML 源，而 EntityExpansionLimit 将允许应用程序将内存消耗控制在可接受的水平。
在本教程中，您将了解限制，并学习如何正确使用它们。


处理限制定义
以下列表描述了 JDK 中支持的 JAXP XML 处理限制。这些限制可以通过工厂 API、系统属性和jaxp.properties文件指定 。

- 实体扩展限制
属性	描述
姓名	http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit
定义	限制实体扩展的数量。
价值	一个正整数。小于或等于 0 的值表示没有限制。如果该值不是整数，则抛出NumericFormatException 。
默认值	64000
系统属性	jdk.xml.entityExpansionLimit
自从	7u45, 8

- 元素属性限制

  
    属性	描述
    姓名	http://www.oracle.com/xml/jaxp/properties/elementAttributeLimit
    定义	限制一个元素可以拥有的属性数量。
    价值	一个正整数。小于或等于 0 的值表示没有限制。如果该值不是整数，则抛出NumericFormatException 。
    默认值	10000
    系统属性	jdk.xml.elementAttributeLimit
    自从	7u45, 8

- 最大出现次数


    属性	描述
    姓名	http://www.oracle.com/xml/jaxp/properties/maxOccurLimit
    定义	限制在为 W3C XML 架构构建语法时可能创建的内容模型节点的数量，该架构包含maxOccurs属性，其值不是“unbounded”。
    价值	一个正整数。小于或等于 0 的值表示没有限制。如果该值不是整数，则抛出NumericFormatException 。
    默认值	5000
    系统属性	jdk.xml.maxOccurLimit
    自从	7u45, 8
- 总实体大小限制


    属性	描述
    姓名	http://www.oracle.com/xml/jaxp/properties/totalEntitySizeLimit
    定义	限制包括一般和参数实体在内的所有实体的总大小。大小计算为所有实体的聚合。
    价值	一个正整数。小于或等于 0 的值表示没有限制。如果该值不是整数，则抛出NumericFormatException 。
    默认值	5x10^7
    系统属性	jdk.xml.totalEntitySizeLimit
    自从	7u45, 8
- 最大通用实体大小限制


    属性	描述
    姓名	http://www.oracle.com/xml/jaxp/properties/maxGeneralEntitySizeLimit
    定义	限制任何一般实体的最大大小。
    价值	一个正整数。小于或等于 0 的值表示没有限制。如果该值不是整数，则抛出NumericFormatException 。
    默认值	0
    系统属性	jdk.xml.maxGeneralEntitySizeLimit
    自从	7u45, 8
- 最大参数实体大小限制


    属性	描述
    姓名	http://www.oracle.com/xml/jaxp/properties/maxParameterEntitySizeLimit
    定义	限制任何参数实体的最大大小，包括嵌套多个参数实体的结果。
    价值	一个正整数。小于或等于 0 的值表示没有限制。如果该值不是整数，则抛出NumericFormatException 。
    默认值	1000000
    系统属性	jdk.xml.maxParameterEntitySizeLimit
    自从	7u45, 8
- 实体替换限制


    属性	描述
    姓名	http://www.oracle.com/xml/jaxp/properties/entityReplacementLimit
    定义	限制所有实体引用中的节点总数。
    价值	一个正整数。小于或等于 0 的值表示没有限制。如果该值不是整数，则抛出NumericFormatException 。
    默认值	300万
    系统属性	jdk.xml.entityReplacementLimit
    自从	7u111, 8u101
- 最大元素深度


属性	描述


    姓名	http://www.oracle.com/xml/jaxp/properties/maxElementDepth
    定义	限制最大元素深度。
    价值	一个正整数。小于或等于 0 的值表示没有限制。如果该值不是整数，则抛出NumericFormatException 。
    默认值	0
    系统属性	jdk.xml.maxElementDepth
    自从	7u65、8u11
- 最大 XML 名称限制


    属性	描述
    姓名	http://www.oracle.com/xml/jaxp/properties/maxXMLNameLimit
    定义	限制 XML 名称的最大大小，包括元素名称、属性名称和名称空间前缀和 URI。
    价值	一个正整数。小于或等于 0 的值表示没有限制。如果该值不是整数，则抛出NumericFormatException 。
    默认值	1000
    系统属性	jdk.xml.maxXMLNameLimit
    自从	7u91, 8u65
- 遗留系统属性

这些属性自 JDK 5.0 和 6 起引入，继续支持向后兼容。

    系统属性	自从	新系统属性
    entityExpansionLimit	1.5	jdk.xml.entityExpansionLimit
    elementAttributeLimit	1.5	jdk.xml.elementAttributeLimit
    maxOccurLimit	1.6	jdk.xml.maxOccur

- {java.home}/lib/jaxp.properties
可以在jaxp.properties文件中指定系统属性，以定义 JDK 或 JRE 的所有调用的行为。格式为system-property-name=value。例如：

  jdk.xml.maxGeneralEntitySizeLimit=1024

### 范围和顺序
XML 处理器（包括 DOM、SAX、模式验证、XSLT 和 XPath）需要 javax.xml.XMLConstants#FEATURE_SECURE_PROCESSING (FSP) 功能。当 FSP 设置为 true 时，将强制执行建议的默认限制。将 FSP 设置为 false 不会更改限制。

当 Java 安全管理器存在时，FSP 设置为 true 且无法关闭。因此，建议的默认限制被强制执行。

jaxp.properties 文件中指定的属性会影响 JDK 和 JRE 的所有调用，并将覆盖它们的默认值或可能已由 FSP 设置的值。

设置系统属性后，会影响 JDK 和 JRE 的调用，并覆盖默认设置或在 jaxp.properties 中设置的设置，或可能已由 FSP 设置的设置。

通过 JAXP 工厂或 SAXParser 指定的 JAXP 属性优先于系统属性、jaxp.properties 文件以及 FEATURE_SECURE_PROCESSING。

### 使用这些限制
- 环境评价

评估包括，在系统级别，应用程序可用的内存量，是否接受和处理来自不受信任来源的 XML、XSD 或 XSL 源，以及在应用程序级别，是否使用某些结构，例如 DTD。

- 内存设置和限制

XML 处理可能非常占用内存。应允许消耗的内存量取决于特定环境中应用程序的要求。必须防止处理格式错误的 XML 数据消耗过多的内存。

默认限制通常设置为允许大多数应用程序使用合法的 XML 输入，允许小型硬件系统（例如 PC）使用内存。建议将限制设置为尽可能小的值，以便在消耗大量内存之前捕获任何格式错误的输入。

这些限制是相关的，但并非完全多余。您应该为所有限制设置适当的值：通常限制应设置为比默认值小得多的值。

例如，可以设置ENTITY_EXPANSION_LIMIT和GENERAL_ENTITY_SIZE_LIMIT来防止过多的实体引用。但是当扩展和实体大小的确切组合未知时，TOTAL_ENTITY_SIZE_LIMIT可以作为一个整体控制。类似地，虽然TOTAL_ENTITY_SIZE_LIMIT控制替换文本的总大小，
但如果文本是非常大的 XML 块，则 ENTITY_REPLACEMENT_LIMIT 对可以出现在文本中的节点总数设置限制并防止系统过载。

使用 getEntityCountInfo 属性估计限制
为了帮助您分析应该为限制设置什么值，可以使用一个名为"http://www.oracle.com/xml/jaxp/properties/getEntityCountInfo" 的特殊属性。以下代码片段显示了使用该属性的示例：

parser.setProperty("http://www.oracle.com/xml/jaxp/properties/getEntityCountInfo", "yes");
有关下载示例代码的更多信息， 请参阅 示例。

当程序使用 W3C MathML 3.0 中的 DTD 运行时，它会打印出下表：


    属性	限制	总尺寸	尺寸	实体名称
    ENTITY_EXPANSION_LIMIT	64000	1417	0	null
    MAX_OCCUR_NODE_LIMIT 个	5000	0	0	null
    ELEMENT_ATTRIBUTE_LIMIT	10000	0	0	null
    TOTAL_ENTITY_SIZE_LIMIT 个	50000000	55425	0	null
    GENERAL_ENTITY_SIZE_LIMIT	0	0	0	null
    PARAMETER_ENTITY_SIZE_LIMIT	1000000	0	7303	%MultiScriptExpression
    MAX_ELEMENT_DEPTH_LIMIT	0	2个	0	null
    MAX_NAME_LIMIT	1000	13	13	null
    ENTITY_REPLACEMENT_LIMIT	300万	0	0	null

在此示例中，实体引用总数或实体扩展为 1417；默认限制为 64000。所有实体的总大小为 55425；默认限制为50000000。最大的参数实体是%MultiScriptExpression，在所有引用解析后长度为7303；默认限制为 1000000。

如果这是应用程序预期处理的最大文件，建议将限制设置为较小的数字。例如，ENTITY_EXPANSION_LIMIT为 2000 ，TOTAL_ENTITY_SIZE_LIMIT为 100000 ，PARAMETER_ENTITY_SIZE_LIMIT为 10000 。

- 设定限制

可以使用与其他 JAXP 属性相同的方式设置限制。它们可以通过工厂方法或通过解析器设置：

```java
DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
dbf.setAttribute(名称, 值);

SAXParserFactory spf = SAXParserFactory.newInstance();
SAXParser 解析器 = spf.newSAXParser();
parser.setProperty(名称, 值);

XMLInputFactory xif = XMLInputFactory.newInstance();
xif.setProperty(名称, 值);

SchemaFactory schemaFactory = SchemaFactory.newInstance(schemaLanguage);
schemaFactory.setProperty（名称，值）；

TransformerFactory factory = TransformerFactory.newInstance();
factory.setAttribute(名称, 值);
以下示例显示如何使用DocumentBuilderFactory设置限制：

dbf.setAttribute(JDK_ENTITY_EXPANSION_LIMIT, "2000");
dbf.setAttribute(TOTAL_ENTITY_SIZE_LIMIT, "100000");
dbf.setAttribute(PARAMETER_ENTITY_SIZE_LIMIT, "10000");
dbf.setAttribute(JDK_MAX_ELEMENT_DEPTH, "100");
```
- 使用系统属性

如果更改代码不可行，系统属性可能会有用。

要为 JDK 或 JRE 的整个调用设置限制，请在命令行上设置系统属性。要仅为应用程序的一部分设置限制，可以在该部分之前设置系统属性并在之后清除。以下代码显示了如何使用系统属性：
```java
public static final String SP_GENERAL_ENTITY_SIZE_LIMIT = "jdk.xml.maxGeneralEntitySizeLimit";

//使用系统属性设置限制
System.setProperty(SP_GENERAL_ENTITY_SIZE_LIMIT, "2000");

//这个设置会影响设置后的所有处理
...

//完成后，清空属性
System.clearProperty(SP_GENERAL_ENTITY_SIZE_LIMIT);
请注意，属性的值应为整数。如果输入的值不包含可解析的整数，将抛出NumberFormatException；看方法 parseInt(String)。

```
有关下载示例代码的更多信息， 请参阅 [示例](https://docs.oracle.com/javase/tutorial/jaxp/limits/sample.html)。

- 使用 jaxp.properties 文件

jaxp.properties 文件是一个配置文件。它通常位于${ java.home }/lib/jaxp.properties，其中java.home是 JRE 安装目录，例如，[安装目录的路径] /jdk8/jre。

可以通过将以下行添加到jaxp.properties文件来设置限制：

jdk.xml.maxGeneralEntitySizeLimit=2000

请注意，属性名称与系统属性的名称相同，并具有前缀jdk.xml。属性的值应为整数。如果输入的值不包含可解析的整数，将抛出NumberFormatException；看方法 parseInt(String)。

当在文件中设置该属性时，JDK 和 JRE 的所有调用都将遵守该限制。

### 异常处理
建议应用程序在设置其中一个新属性时捕获 org.xml.sax.SAXNotRecognizedException 异常，以便应用程序可以在不支持它们的旧版本上正常工作。
例如，可下载的示例代码包含以下方法 isNewPropertySupported，它检测示例是否使用支持 JDK_GENERAL_ENTITY_SIZE_LIMIT 属性的 JDK 版本运行：
```java
public boolean isNewPropertySupported() {
    try {
        SAXParser parser = getSAXParser(false, false, false);
        parser.setProperty(JDK_GENERAL_ENTITY_SIZE_LIMIT, "10000");
    } catch (ParserConfigurationException ex) {
        fail(ex.getMessage());
    } catch (SAXException ex) {
        String err = ex.getMessage();
        if (err.indexOf("Property '" + JDK_GENERAL_ENTITY_SIZE_LIMIT +
                                       "' is not recognized.") > -1) {
            //expected before this patch
            debugPrint("New limit properties not supported. Samples not run.");
            return false;
        }
    }
    return true;
}

```
当输入文件包含导致超限异常的构造时，应用程序可能会检查错误代码以确定故障的性质。为限制定义了以下错误代码：

* EntityExpansionLimit: JAXP00010001
* ElementAttributeLimit: JAXP00010002
* MaxEntitySizeLimit: JAXP00010003
* TotalEntitySizeLimit: JAXP00010004
* MaxXMLNameLimit: JAXP00010005
* maxElementDepth: JAXP00010006
* EntityReplacementLimit: JAXP00010007

错误代码具有以下格式：
```text
"JAXP" + components (two digits) + error category (two digits) + sequence number
```
### stax

StAX、JSR 173 不支持 FSP。然而，JDK 中的 StAX 实现支持新的限制属性及其相应的系统属性。这意味着，虽然没有 FSP 可以打开和关闭限制，但所描述的限制和系统属性的工作方式完全相同。

为了兼容性，StAX 特定属性始终在新的 JAXP 限制之前生效。例如，当 SupportDTD 属性设置为 false 时，会在输入文件包含实体引用时引发异常。因此，添加新限制对使用 SupportDTD 属性禁用 DTD 的应用程序没有影响。