# xml catalog API

使用xml catalog API 去实现本地的 xml 目录 ..

javaSE 9 引入了新的xml catalog API 去支持对[结构化信息标准促进组织(OASIS)的XML 目录](https://www.oasis-open.org/committees/download.php/14809/xml-catalogs.html) ..

这个章节描述了如何使用此API,由Java XML 处理器所支持, 使用表达式 ..

XML Catalog API 是实现本地 catalog的直观的API. 并且由JDK XML 处理器支持让它更容易配置你的处理器或者整体环境去利用此特性的优势 ..

## 了解创建Catalog的更多信息
点击上面的链接,XML catalogs 位于/etc/xml/catalog 目录下(在某些linux 发行版中) 它可以是创建一个本地catalog的一个好的参考 ...

### XML Catalog API的目的

XML Catalog API 以及 Java XML processors 提供了一种选项(让开发者和系统管理者能够去管理外部资源) ..

XML Catalog API 提供了对OASIS XML Catalogs v1.1的实现,一个设计标准去解决由外部资源导致的问题 ..

#### 由外部资源导致的问题

XML,XSD,XSL文档也许会包含一些外部资源的引用(Java XML 处理器需要去抓取来处理文档) ..

外部资源可能会对应用或者系统导致问题 .. 那么Catalog API 以及 Java XML处理器为应用开发者以及系统管理者管理这些外部资源提供了一个选项:

- 可用性

    如果资源是远程的,那么XML处理器必须能够连接到托管此资源的远程服务器 .. 即使连接很少是一个问题 ,它仍然是应用的稳定性的一个因素 ..

    太多的连接可能会对持有资源的服务器是一个危害 .. 并且最终可能会影响到你的应用 .. 查看 [Use Catalog with XML Processors](https://docs.oracle.com/en/java/javase/20/core/xml-catalog-api1.html#GUID-34E80AF6-7D70-49A3-99C4-1B91DB0B3036) 举例 -- 使用XML Catalog API 解决了这个问题 ..
- Performance

  尽管大多数情况可连接性不是一个问题, 一个远程抓取仍然可能导致性能问题(对于一个应用来说), 除此之外,可能存在多个应用在相同系统上尝试解析相同的资源,这可能导致系统资源的滥用 ..

- Security

  允许远程连接可能导致一个安全风险 - 如果应用处理不信任的 XML 资源 ..
- Manageability

  如果系统处理了大量的XML 文档, 那么外部引用的文档,无论是本地或者远程,都可能变成一个维护的麻烦 ..

#### 如何处理由外部资源导致的问题

答案是使用XML Catalog API,应用开发者能够创建一个应用的所有外部引用的本地目录, 并且让Catalog API 解析它们,这不仅避免了远程连接,而且更容易管理这些资源 ..

系统管理员能够建立一个系统的本地目录并配置 java jvm 去使用这个目录.. 然后在系统上的所有应用能够共享相同的目录无需在应用中进行代码修改 .. 假设他们与Java SE9兼容 ..

为了建立一个catalog,你也许能够利用存在的目录的优势(例如包含在某些Linux 发行版中的目录) ..

## XML Catalog API 接口

通过它的接口访问 XML Catalog API

### XML Catalog API 接口

xml catalog api 定义了以下的接口:
- Catalog 接口代表了一个实体目录(由OASIS 定义的), 一个Catalog 对象是不可变的, 在它创建之后,那么Catalog 对象能够被用来在system,public 或者 uri 项中进行匹配发现 ..

  一个自定义的解析者实现也许能够发现它很有用(通过 目录去获取本地资源) ..
- CatalogFeatures 类提供了Catalog API 支持的特性和属性, 包括了 `javax.xml.catalog.files`,`javax.xml.catalog.defer` ,`javax.xml.catalog.prefer` 以及 `javax.xml.catalog.resolve` ..
- CatalogManager 类管理XML 目录的创建 以及 catalog 解析器的创建
- CatalogResolver 接口是一个目录解析器，它实现了 SAX EntityResolver、StAX XMLResolver、由模式验证使用的 DOMLS LSResourceResolver 和转换 URIResolver。此接口使用目录解析外部引用。 

### CatalogFeatures 类的详情

CatalogFeatures 定义了catalog 的特性, 这些特性在API 以及 系统级别上定义 .. 这意味着你能够通过API 以及 系统属性 以及 JAXP 属性设置 .. 为了通过api设置特性,使用CatalogFeatures 类 ..

下面的代码设置`javax.xml.catalog.resolve` 为`continue` ,那么让处理继续,即使没有匹配由CatalogResolver 发现 ...
```java
CatalogFeatures f = CatalogFeatures.builder().with(Feature.RESOLVE, "continue").build();
```

为了设置`continue` 能力到系统范围, 使用 java 命令行 或者 System.setProperty 方法 ..
```java
System.setProperty(Feature.RESOLVE.getPropertyName(), "continue");
```

为了设置`continue` 到 jvm 实例上, 输入一下行到 `jaxp.properties` 文件中
```java
javax.xml.catalog.resolve = "continue"
```

`jaxp.properties` 位于 `$JAVA_HOME/conf` 目录下 ..

此`resolve` 属性,同样`prefer`  以及 `defer` 属性,能够设置为catalog的属性或者在目录文件中作为一个分组项 ..

举个例子,在下面的目录中,`resolve` 属性是设置为`continue` ,这个属性能够设置为`group` 项,如下所示:
```xml
<?xml version="1.0" encoding="UTF-8"?> 
<catalog
  xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog"
  resolve="continue"
  xml:base="http://local/base/dtd/">
  <group resolve="continue">
    <system
      systemId="http://remote/dtd/alice/docAlice.dtd"
      uri="http://local/dtd/docAliceSys.dtd"/> 
  </group>
</catalog>
```
属性设置在更加狭隘的范围将会覆盖更宽范围内定义的一些属性 .. 因此,通过API 设置的属性总是具有更高的优先级 ..

### 使用XML Catalog API
解析在XML 源文档中的DTD,entity 以及额外的URI 引用 - 使用XML Catalog 标准的各种项类型 ..

XML Catalog 标准定义了大量的项类型 .. 在他们之间,系统项,包括了`system` ,`rewriteSystem` ,`systemSuffix` 项 ..将被用来
解析XML 源文档中的DTD 以及 实体引用,然而, `uri` 项负责替代的URI 引用 ..

### System 参考
使用CatalogResolver 对象去获取一个本地资源 ..

考虑以下的XML 文件:
```xml
<?xml version="1.0"?> 
<!DOCTYPE catalogtest PUBLIC "-//OPENJDK//XML CATALOG DTD//1.0" 
  "http://openjdk.java.net/xml/catalog/dtd/example.dtd">

<catalogtest>
  Test &example; entry
</catalogtest>
```
上述xml中引用了一个 项(example) ..

然后`example.dtd` 文件中定义了一个项`example`:
```xml
<!ENTITY example "system">
```
然而,引用example.dtd文件的url在xml文件中不需要存在,此目的是为了提供一个唯一的标识符 让CatalogResolver对象去获取一个本地资源 ..

为了这样做,创建一个叫做catalog.xml的 catalog 项文件(里面包含了`system` entry 引用了一个本地资源):
```xml
<?xml version="1.0" encoding="UTF-8"?> 
<catalog xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog">
  <system
    systemId="http://openjdk.java.net/xml/catalog/dtd/example.dtd"
    uri="example.dtd"/>
</catalog>
```
通过此catalog 项文件以及`system`  项, 对此你需要获取一个默认的`CatalogFeatures` 对象 并设置一个指向catalog 项文件的uri 去创建
CatalogResolver 对象:
```java
CatalogResolver cr =
  CatalogManager.catalogResolver(CatalogFeatures.defaults(), catalogUri);
```

catalogUri 必须是一个有效的URI, 例如:
```java
URI.create("file:///users/auser/catalog/catalog.xml")
```
CatalogResolver 对象现在能够被作为一个 JDK XML resolver, 在下面的示例中
```java
SAXParserFactory factory = SAXParserFactory.newInstance();
factory.setNamespaceAware(true);
XMLReader reader = factory.newSAXParser().getXMLReader();
reader.setEntityResolver(cr);
```

注意到在这个示例中,系统标识符是一个绝对的URI, 这让它能够更容易让解析器去发现和catalog中system项
中的systemId完全相同的匹配 ..

如果此`system` 标识符在xml中是相对的, 这也许会导致匹配处理变得复杂, 因为XML 处理器也许会使用一个指定的base URI
或者 源文件的URI 做出它的绝对URI ..在这种情况下, system项的`systemID` 可能需要匹配预期的绝对URI, 一个容易的解决方案是使用
`systemSuffix` 项,例如:
```xml
<systemSuffix systemIdSuffix="example.dtd" uri="example.dtd"/>
```

`systemSuffix` 项匹配在XML 源中的任何一个以`example.dtd` 结尾的引用 并解析它到 本地`example.dtd` 文件(在uri 属性中所指定的文件).
您可以向 systemId 添加更多内容以确保它是唯一的或正确的引用,例如,你也许能够设置`systemIdSuffix` 到 `xml/catalog/dtd/example.dtd` ,或者
重命名同时在xml 源文件和 `systemSuffix` 项中的 id 去确保他们唯一匹配,举个例子`my_example.dtd` ..

`system`项的URI 能够是绝对或者相对的,如果外部资源有固定的位置,那么绝对URI 更可能保证唯一性 ..
如果外部资源是放置在相对于你的应用或者catalog 项文件,那么相对URI 也许更加灵活, 允许你的应用部署而无需知道外部资源安装在哪里 ..

例如一个相对URI 它将使用base URI 或者 catalog 文件的URI(如果base URI 没有指定)来解析,
在前面的示例中,`example.dtd` 奖假设已经放置在和catalog 文件相同的目录下 ..


#### Public 引用

使用public  项去替代一个`system` 项发现想要的资源 ..

如果没有system 项匹配想要的资源,那么 PREFER 属性将被指定去匹配 public , 然后public 项能够和`system` 项相同, 注意到`public` 会默认设置`PREFER` 属性 ..

#### 使用一个Public 项
当在已经解析的xml文件中的dtd 引用包含了一个public 标识符(例如 "-//OPENJDK//XML CATALOG DTD//1.0"),那么一个public 项
能够是如下内容(在catalog 项文件中):
```xml
<public publicId="-//OPENJDK//XML CATALOG DTD//1.0" uri="example.dtd"/>
```

当你创建并使用一个CatalogResolver 对象(具有此项文件),那么`example.dtd` 通过`publicId` 属性解析 ..

查看 系统参考 了解如何创建一个CatalogResolver 对象 ..

### URI 参考

使用uri 项去发现想要的资源

URI 类型项,包括 uri,rewriteURI,以及 uriSuffix,使用和system类型项类似 ..

#### 使用URI 项

虽然xml catalog 标准偏好system 类型项去解析dtd 引用,其他事情可以使用uri 类型项, 

但是java xml catalog api 并不会做出区分 ..

这是因为现有的 Java XML 解析器的规范，例如 XMLResolver 和 LSResourceResolver，没有给出偏好。 

uri类型项,包括uri,rewriteURI,uriSuffix 能够以和system 类型项类似的方式使用,uri 元素能够定义一个可选的URI 引用（为一个uri 参考  / 引用) ..
在`system` 引用的情况下,它是 `systemId` 属性 ..

你也许能够通过 uri 项替代system 项,尽管`system` 项对于DTD 引用/ 参考 更加通用
```xml
<system
  systemId="http://openjdk.java.net/xml/catalog/dtd/example.dtd"
  uri="example.dtd"/>
```

替换:
```xml
<uri name="http://openjdk.java.net/xml/catalog/dtd/example.dtd" uri="example.dtd"/>
```

虽然`system` 项对于DTD 使用更加频繁, uri 项对于 URI 参考是首选(例如 XSD / XSL 导入或者包括),下一个示例是使用uri项去解析一个XSL 导入 ..

正如XML Catalog API 接口中所描述,XML Catalog API 定义了CatalogResolver 接口继承了 Java XML 解析器(包括
EntityResolver,XMLResolver,URIResolver 以及 LSResolver), 因此 CatalogResolver 对象能够被SAX,DOM,StAX,Schema 验证,同样XSLT 转换 ..

以下的代码创建了一个CatalogResolver 对象(使用默认的特性配置):
```java
CatalogResolver cr =
  CatalogManager.catalogResolver(CatalogFeatures.defaults(), catalogUri);
```

这个代码然后注册了CatalogResolver 对象到 TransformerFactory类的一个实例上(此实例期待一个URIResolver)
```java
TransformerFactory factory = TransformerFactory.newInstance();
factory.setURIResolver(cr);
```
或者能够在Transformer上注册 CatalogResolver 对象:
```java
Transformer transformer = factory.newTransformer(xslSource); 
transformer.setURIResolver(cur);
```

假设XML 源文件包含了一个import 语句导入 xslImport.xsl 到 XSL 源中:
```xml
<xsl:import href="pathto/xslImport.xsl"/>
```

为了解析这个import 引用到这个实际导入文件所在位置, CatalogResolver 对象应该设置到TransformerFactory类实例上(在创建Transformer对象之前),
并且一个uri 项(例如以下内容且需要增加到catalog 项文件中)
```xml
<uri name="pathto/xslImport.xsl" uri="xslImport.xsl"/>
```
关于绝对或相对 URI 的讨论以及系统引用的 systemSuffix, uriSuffix 条目的使用也适用于 uri 条目。

## Java xml 处理器支持

通过标准的java xml 处理器使用 xml 目录特性 ..

xml 目录特性通过 java xml 处理器支持的,包括sax 以及 dom(javax.xml.parsers) 以及 Stax 解析器(javax.xml.stream),schema 验证(javax.xml.validation) 以及
xml 转换(javax.xml.transform) ..

这意味着你不需要在XML 处理器之外创建CatalogResolver 对象 ..

Catalog 文件能够直接注册到java xml 处理器 或者通过系统属性指定 或者 jaxp.properties 指定 ..

xml处理器通过自动的使用catalogs 执行映射 ..

### 启用Catalog 支持

为了启用xml Catalogs的特性到 处理器上, `USE_CATALOG`特性必须设置为 true,且必须提供至少一个文件 ..

#### USE_CATALOG

一个 java xml 处理器 是通过检测 此特性的值来决定 Catalog 特性是否支持 ... 默认设置为true(对所有的jdk xml 处理器),
java xml 处理器会进一步检查 catalog 文件的可用性,并且尝试使用 xml catalog api(仅当 此特性设置为true,并且catalog 文件可用) ..

磁特性由xml catalog api ,系统属性 以及 jaxp.properties 支持, 例如如果 此特性设置为true 并且想要对特定的处理器禁用catalog 支持,
那么你能够通过api 方式为特定的处理器设置特性即可完成, 以下的代码设置此特性到XMLReader 对象上:
```java
SAXParserFactory spf = SAXParserFactory.newInstance();
spf.setNamespaceAware(true);
XMLReader reader = spf.newSAXParser().getXMLReader();
if (setUseCatalog) {
   reader.setFeature(XMLConstants.USE_CATALOG, useCatalog); 
}
```
另一方面,如果整体环境必须关闭 catalog,那么这能够通过配置jaxp.properties 文件完成
```properties
 javax.xml.useCatalog = false;
```
#### javax.xml.catalog.files

此属性是被XML Catalog API 定义的 并且由JDK XML 处理器支持, 连同其他catalog 特性 ..

为了在解析,验证或者转换过程中使用此catalog 特性,那么需要设置处理器的 FILES 属性,通过系统属性或者
直接设置`jaxp.properties` 文件的路径 ..

#### Catalog URI

此uri 必须是一个有效的URI,例如 file:///users/auser/catalog/catalog.xml.

在catalog 文件中的在system / uri 项中的 uri 引用能够是绝对或者相对的,如果他们是相对的,那么他们能够使用catalog文件的 uri或者 指定的base uri 进行解析 ..

### 使用 system / uri 项

当直接使用 xml catalog api 的时候,`system` 以及 `uri` 项同时工作(当使用JDK XML 处理器的对CatalogFeatures类的原生支持) ..

通常情况下,system 项首先会被查询,然后是public 项,如果没有匹配发现,然后处理器会继续查询 uri项 .. 

由于系统和 uri 条目均受支持，因此建议您在选择使用系统条目还是 uri 条目时遵循 XML 规范的习惯。例如，DTD 是用 systemId 定义的，因此系统条目更可取。


### 通过XML 处理器使用Catalog

能够通过各种Java xml 处理器 使用  Xml catalog api ..

xml catalog api 完全被jdk xml 处理器支持, 以下部分描述了怎样对特定类型的处理器启用支持

#### 与dom 一起使用Catalog

为了与DOM一起使用catalog, 设置 DocumentBuilderFactory实例的FILES 属性:
```java
static final String CATALOG_FILE = CatalogFeatures.Feature.FILES.getPropertyName();
DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
dbf.setNamespaceAware(true);
if (catalog != null) {
   dbf.setAttribute(CATALOG_FILE, catalog);
}
```

注意到 catalog 是一个指向catalog的 uri, 例如:
```text
"file:///users/auser/catalog/catalog.xml"
```

更棒的是基于catalog 项文件 解析连同的目标文件,因此文件能够是基于catalog 文件相对解析 .. 

例如,如果以下在catalog 文件中是一个Uri项,那么 XSLImport_html 文件将位于 /users/auser/catalog/XSLImport_html.xsl ..
```xml
<uri name="pathto/XSLImport_html.xsl" uri="XSLImport_html.xsl"/>
```

### 与SAX 使用Catalog

为了在SAX 解析器上使用Catalog 特性,设置 catalog 文件到 SAXParser 实例上:
```java
SAXParserFactory spf = SAXParserFactory.newInstance();
spf.setNamespaceAware(true);
spf.setXIncludeAware(true);
SAXParser parser = spf.newSAXParser();
parser.setProperty(CATALOG_FILE, catalog);
```

在上面的示例中,注意到statement `spf.setXIncludeAware(true)`. 当启用时,任何XInclude 也能使用catalog 解析 ..

给定一个 XML 文件 `XI_simple.xml`:
```xml
<simple> 
  <test xmlns:xinclude="http://www.w3.org/2001/XInclude"> 
    <latin1>
      <firstElement/>
      <xinclude:include href="pathto/XI_text.xml" parse="text"/>
      <insideChildren/>
      <another>
        <deeper>text</deeper>
      </another>
    </latin1>
    <test2>
      <xinclude:include href="pathto/XI_test2.xml"/> 
    </test2>
  </test>
</simple>
```
除此之外,给定另一个文件 XI_test2.xml:
```xml
<?xml version="1.0"?>
<!-- comment before root -->
<!DOCTYPE red SYSTEM "pathto/XI_red.dtd">
<red xmlns:xinclude="http://www.w3.org/2001/XInclude">
  <blue>
    <xinclude:include href="pathto/XI_text.xml" parse="text"/>
  </blue>
</red>
```
假设另一个文本文件, XI_text.xml 包含了一个简单字符串,并且XI_red.dtd 如下所示:
```xml
 <!ENTITY red "it is read">
```

在这些xml文件中, XInclude 元素中包含了一个XInclude 元素 ..  并且包含了一个DTD的引用 ..

假设他们位于和catalog 文件 - CatalogSupport.xml 相同目录中,增加下面的catalog 项去映射他们:
```xml
<uri name="pathto/XI_text.xml" uri="XI_text.xml"/>
<uri name="pathto/XI_test2.xml" uri="XI_test2.xml"/>
<system systemId="pathto/XI_red.dtd" uri="XI_red.dtd"/>
```

当解析器`parser.parse` 方法调用去解析 XI_simple.xml 文件, 它能够获取在XI_simple.xml文件中的XI_test2.xml 文件 以及 XI_test.xml文件 以及
在XI_test2.xml文件中的 XI_red.dtd ..(通过指定的catalog 解析) ..

### 与StAX使用Catalog

为了通过StAX 解析器使用catalog 特性,设置 catalog 文件到 XMLInputFactory实例上(在创建XMLStreamReader 对象之前) ..
```xml
XMLInputFactory factory = XMLInputFactory.newInstance();
factory.setProperty(CatalogFeatures.Feature.FILES.getPropertyName(), catalog);
XMLStreamReader streamReader =
  factory.createXMLStreamReader(xml, new FileInputStream(xml));
```

当XMLStreamReader streamReader 对象被用来解析XML 源,以及源中的外部引用 - 它们能够根据catalog中指定的项解析 ..

注意,不像DocumentBuilderFactory类 同时具有setFeature 以及 setAttribute 方法 , XMLInputFactory 类仅仅定义了一个setProperty方法 ..

XML Catalog API 特性包括了 XMLConstants.USE_CATALOG  完全通过setProperty方法设置 ..

例如在XMLStreamReader 对象上禁用USE_CATALOG ..

### 在Schema 验证中使用Catalog

为了使用catalog 去解析在schema中的外部资源,例如 XSD 导入以及包括,在SchemaFactory对象上设置 catalog 即可
```java
SchemaFactory factory =
  SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
factory.setProperty(CatalogFeatures.Feature.FILES.getPropertyName(), catalog);
Schema schema = factory.newSchema(schemaFile);
```
[XMLSchema schema document](https://www.w3.org/2009/XMLSchema/XMLSchema.xsd) 包含了对外部DTD的引用
```xml
<!DOCTYPE xs:schema PUBLIC "-//W3C//DTD XMLSCHEMA 200102//EN" "pathto/XMLSchema.dtd" [
        ...
        ]>
```
以及 对于 xsd 导入:
```xml
<xs:import
  namespace="http://www.w3.org/XML/1998/namespace"
  schemaLocation="http://www.w3.org/2001/pathto/xml.xsd">
  <xs:annotation>
    <xs:documentation>
      Get access to the xml: attribute groups for xml:lang
      as declared on 'schema' and 'documentation' below
    </xs:documentation>
  </xs:annotation>
</xs:import>
```
此示例跟随,为了使用本地资源去优化你的应用性能 - 减少到W3C服务器的调用:

- 包括这些项到目录中(设置到SchemaFactory对象上的catalog)
```xml
<public publicId="-//W3C//DTD XMLSCHEMA 200102//EN" uri="XMLSchema.dtd"/>
<!-- XMLSchema.dtd refers to datatypes.dtd --> 
<systemSuffix systemIdSuffix="datatypes.dtd" uri="datatypes.dtd"/>
<uri name="http://www.w3.org/2001/pathto/xml.xsd" uri="xml.xsd"/>
```
- 下载 XMLSchema.dta,datatypes.dtd以及 xml.xsd的源文件 与catalog 文件一同保存 ..

正如已经讨论过, XML Catalog API 让你使用你洗好的任何项 类型.. 在前者的情况下,替代uri项,
你能够使用以下的任意一种:

  - public entry, 因为在import元素中的namespace 属性将会处理为 `publicId` 元素 ..

  ```xml
  <public publicId="http://www.w3.org/XML/1998/namespace" uri="xml.xsd"/>
  ```

  - system entry

  ```xml
   <system systemId="http://www.w3.org/2001/pathto/xml.xsd" uri="xml.xsd"/>
  ```

> 注意: 
> 当尝试 XML Catalog API的时候, 有些可能有用的是确保示例文件中使用的 URIs 或者 systemIDs 没有指向
> 实际的网络上的资源, 并且特别是不能指向W3C 服务器 .. 这能够让你捕捉一些早期的错误(例如catalog 解析失败),
> 并且避免防止对W3C 服务器造成负担 ..  将他们从不必要的连接中释放 ...
> 
> 在这个主题中的所有示例 以及其他相关XML Catalog API 的示例,都有一个任意的"pathto" 增加到任何URI中(来保持这个目的),
> 因此没有URI 可能会解析到一个外部的W3C 资源 ..


为了使用catalog 去解析一个在xml源中需要被验证的外部资源,设置Catalog 到Validator上:
```xml
SchemaFactory schemaFactory =
  SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
Schema schema = schemaFactory.newSchema();
Validator validator = schema.newValidator();
validator.setProperty(CatalogFeatures.Feature.FILES.getPropertyName(), catalog);
StreamSource source = new StreamSource(new File(xml));
validator.validate(source);
```

### 在Transform中使用Catalog
为了在XSLT 转换程序中使用XML Catalog API,设置catalog 文件到 TransformerFactory 对象上:
```xml
TransformerFactory factory = TransformerFactory.newInstance();
factory.setAttribute(CatalogFeatures.Feature.FILES.getPropertyName(), catalog);
Transformer transformer = factory.newTransformer(xslSource);
```
如果工厂用来创建 Transformer 对象的 XSL 源包含类似于以下内容的 DTD、导入和包含语句：
```xml
<!DOCTYPE HTMLlat1 SYSTEM "http://openjdk.java.net/xml/catalog/dtd/XSLDTD.dtd">
<xsl:import href="pathto/XSLImport_html.xsl"/>
<xsl:include href="pathto/XSLInclude_header.xsl"/>
```
那么下面的catalog 项能够被用来解析这些引用:
```xml
<system
  systemId="http://openjdk.java.net/xml/catalog/dtd/XSLDTD.dtd"
  uri="XSLDTD.dtd"/>
<uri name="pathto/XSLImport_html.xsl" uri="XSLImport_html.xsl"/>
<uri name="pathto/XSLInclude_header.xsl" uri="XSLInclude_header.xsl"/>
```

## 解析器的调用顺序
JDK XML 处理器将会在catalog 解析器之前调用自定义解析器 ..

### 自定义解析器偏好Catalog Resolver

catalog 解析器(由CatalogResolver 接口定义的) 能够被用来解析外部引用(由JDK XML 处理器使用 - 针对已经被设置的catalog 文件) ..

然而,如果一个自定义的解析器提供,那么总是将放在catalog 解析器前面 .. 这意味着 一个 JDK XML 处理器首先调用自定义解析器去尝试解析外部资源 ..

如果解析成功,那么处理器将跳过catalog 解析器并继续 .. 仅当没有自定义解析器或者自定义解析器解析返回 null,那么处理器才会调用catalog 解析器 ..

对于使用自定义解析器的应用来说, 这里能够安全的设置额外的catalog 去解析任何资源(自定义解析器不能处理的) ..

对于现存的应用,改变代码是不可行的 ... 然而你能够通过系统属性或者 jaxp.property文件去设置catalog 去重定向外部引用到本地已知的资源(这样的一个不需要和已有的处理交互 - 由自定义解析处理)

## 检测错误

通过隔离问题来检测配置问题。

XML catalog 标准确保处理器需要恢复任何的资源失败 并继续, 因此XML Catalog API 会忽略任何失败的catalog entry 文件(而不会抛出一个错误) ...

这使得更难检测配置问题 ..

### 检测 配置问题
为了检测配置问题, 通过一次设置一个catalog 隔离问题, 设置RESOLVE 到 `strict`, 并且检测 `CatalogException` 是否在不匹配的时候抛出:

- strict(default)

  catalogResolver 行为:

    如果没有匹配指定的引用的资源发现,则抛出CatalogException

  任何不匹配的引用也许都指示一个可能的错误 ..
- continue

  安静退出, 在生产环境下是有用的,保持XML 处理器继续解析任何外部的应用(但是没有被catalog 覆盖的) ..
- ignore

  安静退出, 例如SAX 处理器, 允许跳过外部引用, ignore 值指示CatalogResolver 对象去返回一个空的InputSource, 然后跳过外部引用 ..



