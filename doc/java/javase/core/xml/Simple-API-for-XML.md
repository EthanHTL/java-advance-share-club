# 对于xml的简单api

我们已经知道 sax 是一个事件驱动,串行化访问xml文档的机制 .. 此协议将会被servlet 以及面相网络的程序(
需要传输以及接受xml文档)频繁使用

因为它是最快且较少内存占用的机制 - 来处理 xml 文档,相比(除了)StAX ..

>
> 注意:
> 简而言之,SAX 是面向为了状态独立处理,一个元素的处理不依赖前面遇见的元素 ..
> StAX,另一方面,是面向状态依赖处理,查看如何使用SAX ..


配置一个程序去使用SAX 需要相比于配置使用DOM 多一点点的工作 ... SAX 是一个事件驱动模型(
你提供此回调方法,当读取xml数据的时候,然后解析器执行他们),
并且这使得它更难以可视化 .. 最终,你不能返回到文档的早期部分 .. 或者重新编排它.

对此,开发者如果是编写一个面向用户的应用(需要展示一个xml文档并可能修改它则需要使用DOM 机制) ..

然而,即使你计划相应的构建DOM 应用,这里也有一些重要的理由 让你熟悉SAX 模型 ..

- Same Error Handling

  由SAX / DOM api 生成的相同类型的异常 .. 因此错误处理代码几乎是等价的 ..

- Handling Validation Errors

  默认来说,规范需要验证错误被忽略 ... 如果你想要在验证错误的事件中抛出一个异常(你可能需要这么做),然后你需要理解SAX 如何
  进行错误处理 ..
- Converting Existing Data

  转换现有数据：正如您将在文档对象模型中看到的那样，您可以使用一种机制将现有数据集转换为 XML。但是，利用该机制需要了解 SAX
  模型。

## 什么时候使用sax

当转换存在的数据到XML的时候能有助于理解SAX 事件模型 .. 转换过程的关键就是修改存在的应用程序去发布SAX 事件(
当它读取数据的时候) ..

SAX 是快且高效的,但是它的事件模型使得它进行 状态无关过滤时候更有用 .. 举个例子,一个SAX 解析器在应用中调用一个方法(
当一个元素tag 出现的时候,并且当出现文本的时候调用不同的方法) ..

如果处理中你做的是状态无关(意味着 它不依赖前面遇见的元素), 然后SAX 也能正常工作 ..

另一方面,对于状态依赖处理, 当程序需要在元素A下面使用数据做一些事情 ,但是在元素B下做不同的事情 .. 那么一个拉取式的解析器(
例如流式API - StAX)可能是更棒的选择 ..
使用一个拉取式的解析器,你能够获取的文本节点,无论它将是什么,你都可以在代码中的任何点对它进行询问 ..

因此它很容易使用不同的方式处理文本.. 因为你能够在程序中的多个地方处理它 ..

SAX 需要比dom 更少的内存, 因为SAX不需要构造一个内部呈现(例如xml数据的树结构),正如dom所做的那样 .. 相反,SAX
简单的发送数据到应用中(当它读取的时候);
你的应用能够这样做而不管它想要使用它看见的数据做什么 ..

拉取式的解析器以及 SAX api 同时表现为一个串行 I/O 流 .. 你查看数据 - 当数据输入时 .. 但是你不能够返回到一个更早的位置
或者向前跳跃到不同的位置 .. 通常情况,例如解析器会工作的很好(当你简单的想要读取数据以及让应用对它进行处理 ..)

但是当你需要修改XML 结构的时候,特别是当你需要交互式修改 - 一个内存型的结构能够更有好处 .. DOM 是一个这样的模型,然而,尽管DOM
提供了许多有用的能力(对于大规模的文档,例如书籍或者文章),它也需要大量的复杂编码. 这些处理详情将在DOM对应章节详细解释 ..

对于更简单的应用,复杂性是不必要的,对于快速开发 以及更简单的应用来说, 面向对象 xml
编程规范之一,例如[jdom](http://www.jdom.org/) 以及 [dom4j](http://www.dom4j.org/) 可能更有好处 ..

## 使用SAX 解析 XML 文件

下面是一个示例的JAXP 程序,SAXLocalNameCount,它表示仅使用元素的localName组件的元素数量,namespace 名称被忽略了(为了简单)
,下面的示例也展示了如何使用SAX ErrorHandler ..

### 创建骨架

SAXLocalNameCount 程序如下:

```java
package sax;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.*;
import java.io.*;

public class SAXLocalNameCount extends DefaultHandler {

    Hashtable tags;

    static public void main(String[] args) throws Exception {
        String filename = null;

        for (int i = 0; i < args.length; i++) {
            filename = args[i];
            if (i != args.length - 1) {
                usage();
            }
        }

        if (filename == null) {
            usage();
        }

        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SAXParser saxParser = spf.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(new SAXLocalNameCount());
        xmlReader.setErrorHandler(new MyErrorHandler(System.err));
        xmlReader.parse(convertToFileURL(filename));
    }

    public void startDocument() throws SAXException {
        tags = new Hashtable();
    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts)
            throws SAXException {

        String key = localName;
        Object value = tags.get(key);

        if (value == null) {
            tags.put(key, new Integer(1));
        } else {
            int count = ((Integer) value).intValue();
            count++;
            tags.put(key, new Integer(count));
        }
    }

    public void endDocument() throws SAXException {
        Enumeration e = tags.keys();
        while (e.hasMoreElements()) {
            String tag = (String) e.nextElement();
            int count = ((Integer) tags.get(tag)).intValue();
            System.out.println("Local Name \"" + tag + "\" occurs "
                    + count + " times");
        }
    }

    private static String convertToFileURL(String filename) {
        String path = new File(filename).getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file:" + path;
    }

    private static void usage() {
        System.err.println("Usage: SAXLocalNameCount <file.xml>");
        System.err.println("       -usage or -help = this message");
        System.exit(1);
    }

    private static class MyErrorHandler implements ErrorHandler {

        private PrintStream out;

        MyErrorHandler(PrintStream out) {
            this.out = out;
        }

        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();

            if (systemId == null) {
                systemId = "null";
            }

            String info = "URI=" + systemId + " Line="
                    + spe.getLineNumber() + ": " + spe.getMessage();

            return info;
        }

        public void warning(SAXParseException spe) throws SAXException {
            out.println("Warning: " + getParseExceptionInfo(spe));
        }

        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }
}
```

### 导入类

javax.xml.parsers 包包含了SAXParserFactory 类(用来创建解析器实例),
当无法产生匹配指定配置选项的解析器时将抛出ParserConfigurationException ..

javax.xml.parsers 包也包含了SAXParser 类, 它是由工厂返回且用来解析的 ..

org.xml.sax 包定义了所有被SAX解析器所使用的接口 ..

org.xml.sax.helpers 包包含 DefaultHandler，它定义了将处理解析器生成的 SAX 事件的类。需要 java.util 和 java.io
中的类来提供哈希表和输出。

### 配置 I/O

在前面完整的示例中我们能够发现,它通过处理命令行参数来执行xml 解析, 那么文件名作为程序的输入

以下代码表示SAXLocalNameCount 如何处理文件 ..

```java
static public void main(String[]args)throws Exception{
        String filename=null;

        for(int i=0;i<args.length;i++){
        filename=args[i];
        if(i!=args.length-1){
        usage();
        }
        }

        if(filename==null){
        usage();
        }
        }
```

能够发现当出现问题的时候直接抛出异常,现在需要验证命令行参数是否为有效的文件名称输入:

```java
public class SAXLocalNameCount {
    private static String convertToFileURL(String filename) {
        String path = new File(filename).getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file:" + path;
    }

    // ...
}
```

上述代码中将filename 参数转换为 java.io.File URL(通过内部方法), convertToFile函数(此应用的函数) ..

如果不正确的命令行参数指定,那么 usage方法将执行,此时可以打印不正确的选项到屏幕上 ..

```java
private static void usage(){
        System.err.println("Usage: SAXLocalNameCount <file.xml>");
        System.err.println("       -usage or -help = this message");
        System.exit(1);
        }
```

### 实现ContentHandler 接口

最重要的接口就是 ContentHandler, 此接口包含了SAX 解析器响应各种解析事件所需要执行的函数 ..

主要的事件处理方法是:
`startDocument`,`endDocument`,`startElement` ,`endElement` ..

实现此接口的最容易的方式是 继承DefaultHandler 类 .. 定义在org.xml.sax.helpers 包中 ..
此类对所有的ContentHandler事件都实现为不做任何事情 ..

```java
public class SAXLocalNameCount extends DefaultHandler {
    // ...
} 
```

> 注意到DefaultHandler 也定义了其他主要事件的空方法实现..(例如定义在DTDHandler,EntityResolver
> 以及ErrorHandler接口中的方法) ..

这些方法都需要抛出一个SAXException,在这里抛出的异常将会返回给解析器,然后将异常发送到调用解析器的代码。

### 处理内容事件

当start tag / end tag 出现的时候,tag的名称将会作为字符串传入到startElement 或者 endElement方法中 ..

当start tag 出现的时候,此元素上定义的任何属性也会传入到Attributes 列表中 ..
在元素中发现的字符串将传入到字符串数组中,连同字符串长度以及偏距到数组中指示第一个字符串位置 ..

#### 文档事件

```java
public class SAXLocalNameCount extends DefaultHandler {

    private Hashtable tags;

    public void startDocument() throws SAXException {
        tags = new Hashtable();
    }

    public void endDocument() throws SAXException {
        Enumeration e = tags.keys();
        while (e.hasMoreElements()) {
            String tag = (String) e.nextElement();
            int count = ((Integer) tags.get(tag)).intValue();
            System.out.println("Local Name \"" + tag + "\" occurs "
                    + count + " times");
        }
    }

    private static String convertToFileURL(String filename) {
        // ...
    }

    // ...
}
```

startDocument方法创建了一个哈希表.. 其中的Element Events将被填充为解析器在文档中发现的XML元素。
当解析器到达了文档的结尾, endDocument方法将会执行, 为了获得在哈希表中元素的名称和数量
并打印出一个消息到屏幕上告诉用户每一个元素出现次数 ...

ContentHandler 的方法都需要抛出SAXException,详情了解配置错误处理 ..

### 元素事件

正如在document事件处理中提到,在startDocument方法中创建的哈希表将通过解析器在文档中发现的各种元素进行填充 ..
下面的代码处理start-element 事件:

```java
public void startDocument()throws SAXException{
        tags=new Hashtable();
        }

public void startElement(String namespaceURI,
        String localName,
        String qName,
        Attributes atts)
        throws SAXException{

        String key=localName;
        Object value=tags.get(key);

        if(value==null){
        tags.put(key,new Integer(1));
        }
        else{
        int count=((Integer)value).intValue();
        count++;
        tags.put(key,new Integer(count));
        }
        }

public void endDocument()throws SAXException{
        // ...
        }
```

这个代码处理元素标签,包括定义在start tag中的任何属性,去获取命名空间统一资源标识符(URI),local名称 以及元素的限定的名称 ..

startElement方法将会使用本地名称 以及 它出现次数进行填充 . 注意到当startElement方法执行的时候,
如果命名空间处理没有启动,那么元素的本地名称以及属性结果可能是空字符串 ..

那么代码的处理是使用限定名称而不是简单名称 ..

### 字符事件

JAXP SAX API 允许你处理解析器发送到应用的字符事件, 使用ContentHandler.characters() 方法 ..

> 字符事件并没有在这个示例中说明 ..

解析器不需要一次返回特定数量的字符串 .. 一个解析器能够一次到几百次从单个字符中返回任何事情 ..
并且仍然是一个遵循标准的实现 ...

因此如果你的应用 需要处理它看见的字符串, 它希望使用characters()方法去计算在java.lang.StringBuffer中的数量
并且在它们已经完全发现时处理 ..

你希望当元素结束的时候解析文本, 因此通常你会在此时执行字符串处理,但是你可能也想要在元素开始的时候处理文本 ..
对于文档风格的数据这是必要的, 有一些能够包含与文本混合的XML元素 ..

例如,考虑以下文档碎片:

```xml

<para>This paragraph contains <bold>important</bold> ideas.
</para>
```

最初的文本是 "This paragraph contains" 由<bold> 元素中断 .. importment 文本通过end标签中断 ..

为了严格准确, 字符串处理器应该扫描 符号字符(&) 以及 左箭头括号字符(<)  并且使用&amps 或者&lt; 进行合适的替换 ..

### 处理特殊字符

在 xml中,一个实体是XML 结构(或者简单文本)且有一个名称,通过名称引用实体可能会导致元素能够插入到文本中 以取代实体引用 ..
为了创建一个实体引用,你应该使用一个符号和分号围绕实体名称:

```text
&entityName;
```

当你处理到XML或者HTML的大块时(包含许多特殊字符的情况下,你能够使用CDATA 部分),一个CDATA
部分工作类似于HTML中的<code>...</code>, 仅更多的是:
在CDATA部分中的空白格是重要的,在这个部分中的字符串将不会被解析为XML .. 一个CDATA部分以<![CDATA[and ends with]]> ..

一个示例是:

```html

<p>
    <termdef id="dt-cdsection" term="CDATA Section"
    <term>CDATA sections</term>
    may occur anywhere character data may occur; they are used to escape blocks of text containing characters which
    would otherwise be recognized as markup. CDATA sections begin with the string "<code>&lt;![CDATA[</code>" and end
    with the string "<code>]]&gt;</code>"
</p>
```
一旦解析,这个文本将显示为:
```text
CDATA sections may occur anywhere character data may occur; they are used to escape blocks of text containing characters which would otherwise be recognized as markup. CDATA sections begin with the string "<![CDATA[" and end with the string "]]>".
```
CDATA 的存在使得 XML 的正确回显有点棘手。 如果将要输出的文本不在CDATA 部分中, 那么任何三角括号,符号(&) 以及在文本中的其他特殊字符都会使用合适的实体引用进行替换 ..
(替代左三角括号和&是非常重要的,其他字符能够正确的解析).. 但是如果输出文本是在CDATA部分,那么替代将不会发生,最终文本看起来像前面的示例那样 ..

在简单的程序(例如 SAXLocalNameCount 应用),这不是特别严重 .. 但是需要XML 过滤应用将想要保持对文本的跟踪(不管它是否出现在CDATA中), 因此它们能够正确的处理特殊字符 ..

### 配置解析器
下面的代码将配置解析器并让它开始:
```java
static public void main(String[] args) throws Exception {

    // Code to parse command-line arguments 
    //(shown above)
    // ...

    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setNamespaceAware(true);
    SAXParser saxParser = spf.newSAXParser();
}
```
上面的代码创建了SAXParserFactory 工厂实例, 通过`javax.xml.parsers.SAXParserFactory` 系统属性确定 .. 这个工厂将创建去配置支持xml 命名空间,
然后通过执行工厂的 newSAXParser()方法获取SAXParser 实例 ..

> 注意:
> javax.xml.parsers.SAXParser 类将是一个包装器(定义了大量的便捷方法),它将包装org.xml.sax.Parser对象(某些不怎么友好的),如果需要,你能够通过SAXParser的
> getParser()方法去获取此解析器 ..

你需要实现XMLReader(所有解析器必须实现的),应用程序使用 XMLReader 来告诉 SAX 解析器它要对相关文档执行什么处理。
XMLReader 将在以下代码中实现:
```java
// ...
SAXParser saxParser = spf.newSAXParser();
XMLReader xmlReader = saxParser.getXMLReader();
xmlReader.setContentHandler(new SAXLocalNameCount());
xmlReader.parse(convertToFileURL(filename));
```

这里,你能够通过执行SAXParser的getXMLReader方法获取XMLReader 实例 ..
XMLReader 然后注册SAXLocalNameCount 类作为它的内容处理器 .. 
因此由解析器执行的动作将会是在前面展示的那些事件 ...

最终,XMLReader 告诉解析哪一个文档将会解析(根据传递给它的xml文件的位置),由convertToFileURL方法生成的 File URL形式 ..)

### 配置错误处理

现在你已经能够使用你的解析器,但是现在可以更安全的实现某些错误处理 .. 此解析器将生成三种错误(致命错误,一种错误 / 一种警告) .. 当致命错误发生的时候,解析器不能够继续 ..

因此如果应用不生成异常,那么默认的错误事件处理器将生成一个 ..  但是对于非致命的错误以及警告 .. 默认错误处理器将不会生成异常 .. 并且没有消息将会显示 ..

正如前面的文档事件所展示的那样, 应用的事件处理方法将抛出 SAXException ..

举个例子, startDocument方法的签名 定义如下,需要返回一个SAXException:
```java
public void startDocument() throws SAXException { /* ... */ }
```

一个SAXException 能够构造(通过使用一个消息, 另一个异常 或者同时提供) ..

因为默认的解析器仅仅对致命错误生成异常 .. 并且因为有关错误的信息是由默认的解析器提供的是有限的 .. 
上面的SAXLocalNameCount 程序定义了自己的错误处理,通过MyErrorHandler类 ..

```java
xmlReader.setErrorHandler(new MyErrorHandler(System.err));

// ...

private static class MyErrorHandler implements ErrorHandler {
    private PrintStream out;

    MyErrorHandler(PrintStream out) {
        this.out = out;
    }

    private String getParseExceptionInfo(SAXParseException spe) {
        String systemId = spe.getSystemId();

        if (systemId == null) {
            systemId = "null";
        }

        String info = "URI=" + systemId + " Line=" 
            + spe.getLineNumber() + ": " + spe.getMessage();

        return info;
    }

    public void warning(SAXParseException spe) throws SAXException {
        out.println("Warning: " + getParseExceptionInfo(spe));
    }
        
    public void error(SAXParseException spe) throws SAXException {
        String message = "Error: " + getParseExceptionInfo(spe);
        throw new SAXException(message);
    }

    public void fatalError(SAXParseException spe) throws SAXException {
        String message = "Fatal Error: " + getParseExceptionInfo(spe);
        throw new SAXException(message);
    }
}
```
同样,XMLReader 可以配置错误处理器 ..

MyErrorHandler 类实现了标准的org.xml.sax.ErrorHandler 接口 .. 并且定义了一个方法去获取由解析器生成的SAXParseException 实例提供的异常信息,
它包含了getParseExceptionInfo()方法,简单的获取错误发生的行号(在xml 文档中发生错误的行号) 以及系统的标识符(它们通过调用标准的SAXParseException的getLineNumber 以及 getSystemId方法运行 ..) ..

然后将此异常信息馈送到基本 SAX 错误处理方法 error()、warning() 和 fatalError() 的实现中，更新这些方法以发送有关文档中错误的性质和位置的适当消息。

这些能够更新去发送合适的有关本质的消息和文档中错误的位置 ..

#### 处理非致命错误
当一个xml 文档在一个验证约束上失败的时候将出现,如果解析器发生文档无效,那么错误事件将会生成. 这些错误由验证解析器生成, 给定一个文档类型定义(document type definition - DTD) 或者schema ..
当一个文档具有无效tag的时候,或者发现不被允许的tag 或者schema的情况下元素包含了无效数据 ..

最重要的原理是理解非致命错误将默认被忽略,但是如果有验证错误发生在文档中,你可能不想要继续处理它 .. 你可能想要处理这样的错误为致命的 。。

为了 接管错误处理,可以覆盖DefaultHandler方法的默认实现去处理致命错误,非致命错误 以及警告(它们是ErrorHandler的实现) ..

每一个方法都会发送一个SAXParseException 到这样的每一个方法中, 因此生成一个异常(当一个错误发生的时候,并简单的抛出返回) ..

> org.xml.sax.helpers.DefaultHandler 默认没有做什么 ..  (error  / warning()) ,然而fatalError抛出了一个异常 ..
> 当然你可以抛出一个不同的异常,但是当致命错误发生的时候,如果你的应用没有抛出异常,那么SAX 解析器将会抛出 ... XML 规范如此 ..

#### 处理警告
默认情况下，警告也会被忽略。警告是信息性的，只能在存在 DTD 或模式时生成 。
例如，如果某个元素在 DTD 中定义了两次，则会生成警告。这不是非法的，也不会造成问题，但您可能想了解它，因为它可能不是故意的。
根据 DTD 验证 XML 文档将在 此部分中展示。

## 运行SAX Parser 示例而不验证

1. 保存此文件 保存到sax目录下
2. 编译此文件
```java
javac sax/SAXLocalNameCount.java
```
3. 保存示例xml文件 rich_iii.xml 以及 two_gent.xml 文件到 data目录中(这些文件查看 examplefiles目录下的文件)
4. 根据xml文件运行SAXLocalNameCount 程序 ..

  选择rich_iii.xml 文件
  ```shell
   java sax/SAXLocalNameCount data/rich_iii.xml
  ```

XML文件rich_iii.xml包含威廉-莎士比亚戏剧《理查三世》的XML版本。

当你运行此程序的时候,你将得到以下输出
```text
Local Name "STAGEDIR" occurs 230 times
Local Name "PERSONA" occurs 39 times
Local Name "SPEECH" occurs 1089 times
Local Name "SCENE" occurs 25 times
Local Name "ACT" occurs 5 times
Local Name "PGROUP" occurs 4 times
Local Name "PLAY" occurs 1 times
Local Name "PLAYSUBT" occurs 1 times
Local Name "FM" occurs 1 times
Local Name "SPEAKER" occurs 1091 times
Local Name "TITLE" occurs 32 times
Local Name "GRPDESCR" occurs 4 times
Local Name "P" occurs 4 times
Local Name "SCNDESCR" occurs 1 times
Local Name "PERSONAE" occurs 1 times
Local Name "LINE" occurs 3696 times
```

5. 打开rich_iii.xml 文件

  为了检查错误处理是否工作,删除xml中的一项关闭标签 ... 例如删除了</PRESONA> 在21行 ..
  ```text
   21 <PERSONA>EDWARD, Prince of Wales, afterwards King Edward V.</PERSONA>
  ```
6.再次运行

  此时你将发现错误
  ```text
   Exception in thread "main" org.xml.sax.SAXException: Fatal Error: URI=file:data/rich_iii.xml Line=21: The element type "PERSONA" must be terminated by the matching end-tag "</PERSONA>".
  ```

  正如你所见,当错误出现的时候,解析器将生成一个SAXParseException, 它是一个SAXException的子类(标识了文件以及错误发生的位置 ..)
  
### 实现SAX 验证

SAXLocalNameCount 示例程序使用了一个无验证解析器, 但是也能够激活验证,激活验证允许应用去告诉xml 文档是否包含正确的标签或者 这些标签处于正确的序列中 ..
换句话说,它能够告诉你文档是否有效 .. 如果验证是没有激活的, 无论怎样,它能告诉你文档是否是好的, 正如之前部分所展示的那样(当你从xml元素中删除了一个关闭标签) ..

为了让验证是可能的,xml 文档需要关联一个DTD 或者 XML schema .. 这些选项在示例程序中也是可能的 ..

#### 选择解析器实现

如果没有其他工厂类指定,那么默认的SAXParserFactory 类将被使用 .. 为了让解析器使用一个不同制造商产生的解析器 .. 你能够改变环境变量去指示对应的制造商工厂 ..

例如,你能够填充命令行参数:
```java
java -Djavax.xml.parsers.SAXParserFactory=yourFactoryHere [...]
```
指定的工厂名称必须是完整的限定的类名(所有包前缀必须包括) .. 对于更多信息 .. 查看SAXParserFactory类中的newInstance方法的文档 ..

#### 使用验证解析器

到此时,我们将使用一个验证解析器去发现会发生什么(当你在这个示例程序中使用它的时候) ..

有关验证解析器的两个事情需要理解:
- 一个schema 或者 DTO 是必须的 ..
- 由于schema 或者 dtd 出现, 那么ContentHandler.ignorableWhitespace() 方法将会执行(当无论什么时候可能)

#### 忽略空白格

当一个DTD 出现, 解析器将不再对空白格调用characters() 方法 (它知道是不相关的).

从只处理xml数据的应用来说,这是一件好事,因为应用不需要纯粹为了让xml 可读而对存在的空白格感到困扰 ...

在另一方面,如果编写了一个需要过滤xml数据文件的应用 并且你想要输出一个完全可读的文件版本 .. 那么空白格则是无关紧要的: 它将是必须的

为了获得那些字符串, 你将需要增加ignorableWhitespace方法到应用中, 为了处理任何通常可忽略的空白格(解析器所看到的), 你需要增加一些事情,如下代码实现了
ignorableWhitespace 事件处理器:
```java
public void ignorableWhitespace (char buf[], int start, int length) throws SAXException { 
    emit("IGNORABLE");
}
```

这个代码简单的生成了一个消息让你知道可忽略的空白格已经被看见 .. 然而,并不是所有的解析器会创建完全相同的消息 ..
SAX 规范不需要此方法如何执行, JAVA xml 实现会在dtd 存在的时候这样做 ..

#### 配置此工厂
SAXParserFactory 需要配置去使用一个验证解析器去替代默认的无验证解析器 .. 下面的代码展示了如何配置此工厂,因此它实现了验证解析器:
```java
static public void main(String[] args) throws Exception {

    String filename = null;
    boolean dtdValidate = false;
    boolean xsdValidate = false;
    String schemaSource = null;

    for (int i = 0; i < args.length; i++) {

        if (args[i].equals("-dtd")) {
            dtdValidate = true;
        }
        else if (args[i].equals("-xsd")) {
            xsdValidate = true;
        } 
        else if (args[i].equals("-xsdss")) {
            if (i == args.length - 1) {
               usage();
            }
            xsdValidate = true;
            schemaSource = args[++i];
        } 
        else if (args[i].equals("-usage")) {
            usage();
        }
        else if (args[i].equals("-help")) {
            usage();
        }
        else {
            filename = args[i];
            if (i != args.length - 1) {
                usage();
            }
        }
    }

    if (filename == null) {
        usage();
    }

    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setNamespaceAware(true);
    spf.setValidating(dtdValidate || xsdValidate);
    SAXParser saxParser = spf.newSAXParser();

    // ... 
}
```
这里,SAXLocalNameCount 程序将配置获取额外的参数,这将告诉它去实现无验证,DTD 验证,XML Schema Definition(XSD) 验证 或者XSD 验证 - 根据指定的schema 源文件 ..
(这些选项的描述,-dtd,-xsd 以及 -xsdss 也增加到了usage() 方法中, 但是没有在这里展示) .. 

然后此工厂配置去能够产生合适的验证解析器(当newSAXParser方法执行的时候),正如在配置解析器中发现,你能够使用 `sestNamespaceAware(true)` 去配置工厂返回命名空间感知的解析器 ..

Oracle的实现支持任何配置选项的合并 ..(如果一个合并是被特定的实现不支持, 它需要生成一个工厂配置错误)

#### 验证 XML Schema
尽管XML schema的完全处理超出了这个指南的范围, 这个不分展示了你能够去验证xml文档的步骤(使用xml schema 语言编写的一个现存的schema) ..

为了了解xml schema的更多信息,你能够 回顾在线教程,xml schema 0部分:  初学者 - [http://www.w3.org/TR/xmlschema-0/](http://www.w3.org/TR/xmlschema-0/)

> 注意到这里包含了多种schema 定义语言,包括RELAXNG,Schematron 以及 w3c "xml schema" 标准 ..(甚至一个DTD 可以合法的作为"schema",尽管它是唯一一个没有使用xml 语法去描述schema 约束).
> 然而,"xml schema" 带给我们一种术语挑战 .. 尽管"xml Schema schema"短语 能够很精细,我们将使用"XML Schema definition" 来避免出现冗余 ..

为了通知在一个xml文档中的验证错误,那么解析器工厂必须配置去创建一个验证解析器 ... 除此之外,以下条件必须成立:
- 合适的属性必须设置到SAX 解析器
- 合适的错误处理器必须设置
- 文档必须关联一个schema 

#### 设置SAX Parser Properties
通过定义约束来帮助开始配置属性,上述的示例将具有以下约束(通过设置属性):
```java
public class SAXLocalNameCount extends DefaultHandler {

    static final String JAXP_SCHEMA_LANGUAGE =
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    
    static final String W3C_XML_SCHEMA =
        "http://www.w3.org/2001/XMLSchema";

    static final String JAXP_SCHEMA_SOURCE =
        "http://java.sun.com/xml/jaxp/properties/schemaSource";
}
```

> 注意到 解析器工厂必须配置去生成一个命名空间感知,且能够验证的解析器,有关提供的命名空间详细信息可以在DOM 中发现, 理解schema 验证是一个面向命名空间的过程 ..
> 因为服从JAXP的解析器默认是不会感知命名空间的 .. 有必要设置此属性去保证schema 验证工作 ..

然后你必须配置解析器去告诉使用哪一个schema 语言,在SAXLocalNameCount中, 验证能够要么根据一个DTD 或者 针对一个 XML Schema进行执行 ...
下面的代码使用上面定义的约束(指定的是w3c的xml schema 语言,当程序开始时如果指定了-xsd 选项,那么使用它) ..
```java
// ...
if (xsdValidate) {
    saxParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
    // ...
}
```
还有上面提到的错误处理,当配置解析器基于schema的验证的时候,可能会出现错误,如果解析器是不和JAXP 规范兼容,并且由于它不支持xml schema,那么它能够抛出SAXNotRecognizedException .
为了处理这种情况,设置Property 语句包装到try/catch block中,正如下面的代码所示:
```java
// ...
if (xsdValidate) {
    try {
        saxParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
    }
    catch (SAXNotRecognizedException x){
        System.err.println("Error: JAXP SAXParser property not recognized: "
                           + JAXP_SCHEMA_LANGUAGE);

        System.err.println( "Check to see if parser conforms to the JAXP spec.");
        System.exit(1);
    }
}
// ...
```

#### 为文档关联一个Schema
使用一个xml schema 定义去验证数据,必要去确定 xml文档已经关联了schema . 这里有两种方式可以做:
1. 通过包括schema 声明在 xml 文档中
2. 通过在应用中指定需要被使用的schema 

> 注意:
> 当应用指定了使用的schema, 它会覆盖在文档中的任何schema 声明

为了在文档中指定的schema 定义,你能够创建xml:
```xml
<documentRoot
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:noNamespaceSchemaLocation='YourSchemaDefinition.xsd'>
```

这第一个属性定义了xml 命名空间(xmlns)前缀, xsi, 这是xml schema 实例的标准 ..
第二行指定了schema 将会使用在文档中没有指定命名空间的元素上 ..
比如说通常定义在任何简单,不复杂的xml文档中的元素 ..

> 注意到: 有关命名空间的更多信息是包括在DOM中的xml schema 验证中 . 对此,将这些属性作为魔术咒语 ..
> 你能够使用它去验证简单的xml 文件(没有使用命名空间的), 在你了解了有关命名空间的更多信息, 你将看到如何使用XML Schema 去验证复杂的文档(使用命名空间的) ..
> 那些想法都在DOM中验证多个命名空间中讨论 ..

你能够在应用中指定一个schema 文件, 在这种情况下(SAXLocalNameCount):
```java
// ...
if (schemaSource != null) {
    saxParser.setProperty(JAXP_SCHEMA_SOURCE, new File(schemaSource));
}

// ...

```

在上面的代码中,变量schemaSource 相关的 schema 源文件可以通过在-xsdss选项中指定去提供被使用的schema 源文件的名称去启用这个示例文件 ..

#### 在验证解析器中的错误处理
重要的四仅仅当文件验证失败的时候会抛出一个异常 - 结果就是执行错误处理代码 ..

```java
// ...

public void warning(SAXParseException spe) throws SAXException {
    out.println("Warning: " + getParseExceptionInfo(spe));
}
        
public void error(SAXParseException spe) throws SAXException {
    String message = "Error: " + getParseExceptionInfo(spe);
    throw new SAXException(message);
}

public void fatalError(SAXParseException spe) throws SAXException {
    String message = "Fatal Error: " + getParseExceptionInfo(spe);
    throw new SAXException(message);
}

// ...
```

如果这些异常没有抛出,那么验证错误将会被简单的忽略 .. 通常,SAX 解析的错误是验证错误,尽管它也能够被生成(
如果文件指定了一个xml的版本 - 解析器无法准备去处理 ..), 记住你的应用将不会生成一个验证异常(除非你提供了上述类似的错误处理器) ....

#### DTD 警告

当SAX 处理DTD的时候会生成警告, 某些警告仅仅被验证解析器生成, 非验证的解析器 主要目的就是尽可能快的操作 ..
但是它也会生成某些警告 ..

XML 规范建议这些警告应该作为以下的结果:
- 为实体，属性或者标记 、符号提供了额外的声明(这样的声明被忽略,仅仅第一个将被使用,同样,注意到元素的重复定义总是产生了一个致命错误 - 当验证的时候) ..
- 引用了一个未声明的元素类型(一个验证性的错误会发生 - 仅当如果未声明的类型实际上在xml文档中使用),一个警告会出现 - 当在DTD中引用了未声明的元素 ..
- 一个未声明的元素类型的属性声明

在其他情况下JAVA XML SAX 解析器也会弹出警告:
-当验证的时候 没有 <!DOCTYPE ...>
- 当没有验证的时候引用了一个未定义的 参数实体, 当验证的时候会出现错误, 尽管非验证解析器是不需要读取参数实体的,java xml 解析器会这样做 .. 因为它不是一个必须的,
java xml 解析器会生成一个警告而不是错误...
- 某些情况下 - 字符编码声明看起来不正确 ..

#### 运行验证的 SAX Parser Examples

在这个部分中,SAXLocalNameCount 示例程序将会验证 XML Schema 或者 DTD .. 这是最好的方式来说明不同类型的验证是修改将要被解析的xml的代码 .. 以及所关联的schema 和DTD ..
去中断处理 并让应用去生成异常 ..

##### 与DTD 验证错误实验

正如上面的状态,这些示例会重用SAXLocalNameCount 程序 ..
1. 先根据之前的示例说明做好准备工作
2. 运行 SAXLocalNameCount 程序, 使用DTD 验证
  
  为了这样做,指定 -dtd 选项
  ```java
    java sax/SAXLocalNameCount -dtd data/rich_iii.xml
  ```
  
  你会发现错误 ..
  ```text
    Exception in thread "main" org.xml.sax.SAXException:
    Error: URI=file:data/rich_iii.xml 
    Line=4: Document is invalid: no grammar found.
  ```
  这个消息表示对于rich_iii.xml文件没有语法 .. 因此验证无效 .. 换句话说,我们需要提供DTD ..
  因为没有DOCTYPE 声明存在, 因此你需要知道DTD 是必须的(为了验证文档) ..
3. 保存示例的 play.dtd 文件
  
  此文件可以在examplefiles目录中发现
4. 打开rich_iii.xml 文件,插入DOCTYPE 声明到此文件的开头 .. 

  此声明指向一个叫做play.dtd的DTD文件 让验证解析器去使用, 如果DTD 验证激活,那么这个xml文件的结构将会被解析并且会根据play.dtd所提供的结构进行检查 ..

  ```xml
    <!DOCTYPE PLAY SYSTEM "play.dtd">
  ```
  最终保存此文件修改 ..
  
5. 返回此文件并 修改第18行的"KING EDWARD The Fourth"字符的标签 ..

  改变开始和结束的标签,从<PERSONA> 以及 </PERSONA> 到<PERSON> 以及</PERSON>,那么看起来如下所示:
  ```text
   18:<PERSON>KING EDWARD The Fourth</PERSON>
  ```

6. 运行激活验证DTD的程序

  此时,你能够发现不同的错误, 元素类型没有声明
  ```text
   java sax/SAXLocalNameCount -dtd data/rich_iii.xml
    Exception in thread "main" org.xml.sax.SAXException: 
    Error: URI=file:data/rich_iii.xml 
    Line=26: Element type "PERSON" must be declared.
  ```

  此时你能够看到解析器有一个元素没有包括在DTD中 .(play.dtd)
  
7. 现在修改正确的标记

8. 在对应的xml文件中删除 <TITLE> Dramatis Personae</TITLE> ..在第16行 ..

9. 继续执行程序 with validation
```text
java sax/SAXLocalNameCount -dtd data/rich_iii.xml
Exception in thread "main" org.xml.sax.SAXException: 
Error: URI=file:data/rich_iii.xml 
Line=77: The content of element type "PERSONAE" must match "(TITLE,(PERSONA|PGROUP)+)".
```
上述的错误已经告诉我们,<PERSONAE> 元素渲染是无效的,因为它不包含一个DTD 所期待的 <PERSONAE>的子元素 ..
这个错误消息陈述了这个错误发生在data/rich_iii.xml的77行 .. 即使你删除了16行的<TITLE> 元素 ..

这是因为<PERSONAE> 元素的关闭标签位于 77行, 解析器仅仅抛出这个异常(当它到达所解析元素的结尾) ...
10.打开DTD 文件 ..

  我们能够看到PERSONAE元素的声明
  ```text
    <!ELEMENT PERSONAE (TITLE, (PERSONA | PGROUP)+)>
  ```
  正如你所见,这个元素需要一个<TITLE> 子元素 .. 管道(|) 键意味着<PERSONA> 或者 PGROUP 子元素可能包括在这个元素中 。。
  后面的(+) 表示 这些元素可以是一个或者多个 ..

11. 增加一个问题标记(?)键在TITLE中
```text
<!ELEMENT PERSONAE (TITLE?, (PERSONA | PGROUP)+)>
```
那么则表示此子元素是可选的 ..

你也可以在此元素后面增加 (*),那么表示0个或者多个 .. 然而,这种情况下在一个文档的一个部分中存在多个title 你没有啥好处 ..

12. 运行程序
```shell
java sax/SAXLocalNameCount -dtd data/rich_iii.xml
```
此时你可以发现验证结果没有任何错误  ..

#### 与Schema 验证错误进行验证

前面针对 DTD 进行验证 xml 文件,在这练习中你使用SAXLocalNameCount 去验证不同的XML 文件(根据标准的XML Schema 定义和自定义schema 源文件) ..

再次,验证的类型将被说明 - 通过打断解析过程 / 通过修改xml文件 以及 schema,因此解析器会抛出错误 ..

1. 保存 personal-schema.xml 文件

  这是一个简单的xml文件 提供了名称以及约定详情(对于一个小公司的员工).. 在这个xml文件中,你能够看到它已经关联了 一个schema 定义文件: personal.xsd
  ```text
   <personnel xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation='personal.xsd'>
  ```
2. 保存 personal.xsd  xsd schema 文件

  此schema 定义了信息的种类(每一个员工所需要的) - 为了让关联了此schema的XML 文档考虑为有效的 ..
  你能够查看每一个person 元素 都有一个名称,并且每一个员工的名称必须包括family 名称和一个 given 名称 .. 员工也可选的包含邮箱和URL地址 ..

3. 在personal.xsd 中改变邮件地址的最小位数位从0 到 1

  那么mail的声明如下
  ```text
  <xs:element ref="email" minOccurs='1' maxOccurs='unbounded'/>
  ```
4. 在personal-schema.xml 中删除 email 元素(从person元素中 - one.worker具有此id的)

  工人 one 现在看起来如下:
```xml
<person id="one.worker">
  <name><family>Worker</family> <given>One</given></name>
  <link manager="Big.Boss"/>
</person>
```
5. 根据personal-schema.xml 运行示例程序,没有schema 验证

```shell
java sax/SAXLocalNameCount data/personal-schema.xml
```
你能够发现在personal-schema.xml中每一个元素出现的次数 ..

你能够查看到 email仅仅出现了5此, 但是本来有6个person 元素 .. 这是因为我们设置了
email 元素的出现最小化次数在每一个person元素上 .. 我们知道这个文档是无效的 。。

然而此程序不会尝试去根据schema 去验证, 所以没有错误发生 ..

6. 再次运行,这次指定此文档需要根据personal.xsd schema 定义进行验证
```shell
java sax/SAXLocalNameCount -xsd data/personal-schema.xml
```
  这次你能够发现错误消息:
```text
Exception in thread "main" org.xml.sax.SAXException: Error: 
URI=file:data/personal-schema.xml 
Line=14: cvc-complex-type.2.4.a: Invalid content was found starting with 
element 'link'. 
One of '{email}' is expected.
```
7. 恢复one.worker的person元素的email元素 ..
8. 尝试运行,此时没有错误发生
9. 打开personal-schema.xml

  删除personnel 元素中有关personal.xsd schema 定义的声明
  从personnel元素中移除掉倾斜的代码
  ```text
  <personnel xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation='personal.xsd'/>
  ```
10. 再次运行
```shell
java sax/SAXLocalNameCount -xsd data/personal-schema.xml
```
很明显,这不会工作,因为需要验证xml文件的 schema 定义没有声明, 你将发现以下错误:
```text
Exception in thread "main" org.xml.sax.SAXException: 
Error: URI=file:data/personal-schema.xml 
Line=2: cvc-elt.1: Cannot find the declaration of element 'personnel'.
```
11. 此时我们在命令行中传递schema 定义文件
```shell
java sax/SAXLocalNameCount -xsdss data/personal.xsd data/personal-schema.xml
```
此时,我们也发现可以通过应用指定一个schema 选项,但是会覆盖所有在xml中声明的schema 引用,同样没有错误发生 ..

### 处理词汇事件

目前为止,你已经消化了许多xml 概念,包括DTD 以及 外部实体 .. 您还学习了使用 SAX 解析器的方法。

这个教程的剩余部分 涵盖了高级主题 - 你将需要去理解(如果你编写基于SAX的应用) ..  如果你的主要目标是编写基于DOM 的应用,那么你应该跳到DOM章节 。。

在早期如果编写文本作为xml, 你需要知道你是否在 CDATA部分中 .. 如果你是,那么单箭头括号(<) 以及 & 应该保持输出不变 ..
但是如果你不是,那么它们应该被预定义的实体进行替换(&lt; 以及 &amp;),但是您如何知道您是否正在处理 CDATA 部分？

再次,如果你需要基于某种方式过滤xml,你想要绕过连同的注释 .. 通常解析器将忽略注释,你怎样获得注释并且如何回应它们?

这个部分回答了这些问题,展示了如何使用org.xml.sax.ext.LexicalHandler 去识别注释 .. CDATA 部分 以及对解析的实体引用 ..

注释,CDATA 标签,以及解析过的引用构成了词法信息,那么就是,XML 自身的文本有关(涉及的)的信息
而不是xml的信息内容 .. 大多数应用,是这样,当然,仅仅关注XML 文档的内容 .. 这样的应用将不需要使用LexicalEventListener API，但是输出XML 文本的应用将发现它是很宝贵的 ..

> 注意到,词法事件处理是一个可选的解析器特性,解析器实现不需要支持它 ..(参考实现是这样做的)

#### 如何让LexicalHandler 工作

当SAX解析器看见了词法信息的时候为了通知到, 你能够配置XMLReader - 使用LexicalHandler配置 ..

此接口定义了以下的事件处理方法:
- comment(String comment)
  
  传递注释到应用
- startCDATA(),endCDATA()

  当CDATA部分开始或者结束的时候告知,这告诉你的应用期待下一次调用characters() 是什么样的字符的类型
- startEntity(String name),endEntity(String name)

  获得已经解析的实体的名称
- startDTD(String name,String publicId,String systemId),endDTD()

  当DTD 将要被处理的时候以及识别它的时候调用


为了激活词法处理器,你的应用必须扩展DefaultHandler 并实现LexicalHandler 接口.. 然后你必须配置你的XMLReader 实例(sax 解析器所委派的),并配置它
能够发送词法事件到你的词法处理器,如下所示
```java
// ...

SAXParser saxParser = factory.newSAXParser();
XMLReader xmlReader = saxParser.getXMLReader();
xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler",
                      handler); 
// ...
```

这里你能够使用XMLReader的setProperty()方法去配置XMLReader, 属性名是定义作为SAX标准的一部分,是一个URN, http://xml.org/sax/properties/lexical-handler

最终,增加某些类似下面的代码去定义合适的方法(覆盖需要实现的接口的方法) ..
```java
// ...

public void warning(SAXParseException err) {
    // ...
}

public void comment(char[] ch, int start, int length) throws SAXException {
    // ...   
}

public void startCDATA() throws SAXException {
    // ...
}

public void endCDATA() throws SAXException {
    // ...
}

public void startEntity(String name) throws SAXException {
    // ...
}

public void endEntity(String name) throws SAXException {
    // ...
}

public void startDTD(String name, String publicId, String systemId)
    throws SAXException {
    // ...
}

public void endDTD() throws SAXException {
    // ...
}

private void echoText() {
    // ...
}

// ...
```

这些代码将转换你的解析应用到一个词法处理器,剩下要做的就是为这些新方法中的每一个提供一个要执行的操作。

### 使用DTDHandler 以及EntityResolver

这部分存在剩余的两个SAX 事件处理器: DTDHandler 以及 EntityResolver. DTDHandler 将在DTD 出现在一个未解析的实体上或者一个标记声明的时候执行,
Entity解析器 开始执行(当URN(public ID) 必须解析为一个URL(system ID)的时候) ..

#### DTDHandler

"选择解析器实现" - 展示了引用包含二进制数据的文件的方法,例如图片文件, 使用MIME 数据类型 ..

这就是最简单的,最具有扩展性的机制 ..  为了与更旧的SGML风格的数据兼容 .. 虽然,它也可以定义一个未解析的实体 ..

NDATA 关键词定义一个未解析的实体:
<!ENTITY myEntity SYSTEM "...URL" NDATA gif>

NDATA关键字说如果在这个实体的数据不是一个可解析的XML 数据,但是相反是使用了某些其他记号的数据. 这种情况下记号命名为gif ..

DTD 必须包括一个对此记号的声明,这可能看起来如下:
```text
<!NOTATION gif SYSTEM "...URL...">
```
当解析器查看到一个未解析的实体或者一个标记声明,它不会使用此信息做啥事,除了传递它给DTDHandler 接口 .. 此接口定义了以下两个方法:
- notationDecl(String name,String publicId,String systemId)
- unparsedEntityDecl(String name,String publicId,String systemId,String notationName)

记号声明方法将传递记号的名称 以及 public / system 标识符 ..(或者同时传递),依赖于在DTD是如何声明的 ..

unparsedEntityDecl 方法传递了实体的名称 .. 合适的标识符,以及它使用的标记的名称 ..
> DTDHandler 接口被DefaultHandler 类实现了 ..

标记能够被用在属性声明 中,举个例子,以下的声明需要GIF / PNG 图片文件格式的标记 ..

```text
<!ENTITY image EMPTY>
<!ATTLIST image ... type NOTATION (gif | png) "gif">
```

这里,这个类型将声明为要么 gif / png,默认情况,如果没有指定,那么就是gif ..

无论是将将记号引用被用来描述一个未解析的实体还是属性,连同应用程序及时做出合适的处理 .. 解析器对符号的语义一无所知。它只传递声明。

#### EntityResolver API
此api 让你可以转换一个public ID(URN) 到system ID(URL) . 你的应用也许需要这样做, 例如,转换例如 href="urn:/someName" 到 "http://someURL"..

此API 仅仅定义了一个方法:
```java
resolveEntity(String publicId,String systemId)
```
此方法返回一个InputSource对象,这将被用来访问实体的内容, 转换一个URL 到InputSource 是足够容易的 ..
但是URL 作为systemID 表示原始文档所在位置,可能是或者可能不是,例如web服务器的某处地方 ..

为了访问一个本地copy,如果存在,你必须维护一个catalog (位于系统某处的) 将映射public id(名称)到本地URL 地址 ..

### 更多信息
下面的链接提供了更多有用的信息

- 了解SAX 标准
  http://www.saxproject.org.
- 有关StAX 拉取式的解析器

  查看 java 社区进程页面(java Community Process page)
  http://jcp.org/en/jsr/detail?id=173.
- Elliot Rusty Harold's introduction:
  http://www.xml.com/pub/a/2003/09/17/stax.html.

- 有关基于schema的验证机制,查看
  The W3C standard validation mechanism, XML Schema:
  http://www.w3.org/XML/Schema.

  RELAX NG's regular-expression-based validation mechanism:
  [Oasis Relax NG TC](https://www.oasis-open.org/committees/tc_home.php?wg_abbrev=relax-ng).

  基于Schematron的断言 验证机制

  http://www.ascc.net/xml/resource/schematron/schematron.html