## Null-Safety
尽管 Java 不允许您通过其类型系统表达 null 安全性，但 Spring Framework 现在在 org.springframework.lang 包中提供了以下注释，让您可以声明 API 和字段的可空性:
* @Nullable 只可以定义在参数、返回值或者字段 表示可能为空
* @NonNull 表示 参数、返回值、字段不能为空(如果应用了@NonNullApi 以及@NonNullFields到参数或者返回值以及字段时,不需要使用此注解)
* @NonNullApi(包级别声明非空作为一个默认语义-对参数或者返回值)
* NonNullFields(包级别的注解声明非空的字段的默认语义) \
Spring Framework 本身利用了这些注解，但它们也可以用于任何基于 Spring 的 Java 项目来声明空安全 API 和可选的空安全字段。尚不支持通用类型参数、可变参数和数组元素可空性，但应在即将发布的版本中提供，有关最新信息，请参阅 SPR-15942。预计可空性声明将在 Spring Framework 版本之间进行微调，包括次要版本。方法体内使用的类型的可空性超出了此功能的范围 
#### jsr 305 元注解
Spring 注释使用 JSR 305 注释（一种休眠但广泛传播的 JSR）进行元注释。 JSR-305 元注释让 IDEA 或 Kotlin 等工具供应商以通用方式提供空安全支持，而无需对 Spring 注释进行硬编码支持。

没有必要也不建议将 JSR-305 依赖项添加到项目类路径以利用 Spring 空安全 API。只有在其代码库中使用空安全注释的基于 Spring 的库等项目才应添加 com.google.code.findbugs:jsr305:3.0.2 和 compileOnly Gradle 配置或 Maven 提供的范围以避免编译警告