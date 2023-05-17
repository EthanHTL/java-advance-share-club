# 使用重定向模型 登录用户到 web 应用
这里所指的是 服务端 web 应用. 使用okta 作为用户存储 ..

## 学习成果
- 创建一个集成 代表应用
- 增加依赖并使用Okta 重定向认证配置应用
- 测试用户登录流程

> 注意,对于单页面(浏览器app),查看 使用重定向模型登录用户到SPA 应用 
> 对于服务器返回非html api 响应,查看[保护 api 端点](https://developer.okta.com/docs/guides/protect-your-api/)

## 配置 Okta
首先配置[Okta 组织](../../../concepts/Okta-organizations.md),Okta 命令行的窗口(CLI) 是做此事的最快方式 .. 如果你不想要安装CLI,能能够手动登录一个组织替代 ..

1. 安装[Okta CLI](https://cli.okta.com/)
2. 如果没有免费的Okta 开发者账户

    a. 打开终端 \
    b. 运行 `okta register`,并键入你的名和姓以及邮箱地址,以及国家 . \
    c. 点击或者点击账户激活邮箱中的激活(Activate) - 这个邮件将发送到你给定的邮箱地址中

    > 如果你没有接收到邮件,检查你的 垃圾邮件过滤器 是否有来自`noreply@okta.com` 的邮件 ..

    d.发现你的新的域  以及一个在邮件中设置密码的连接
    ```text
    Your Okta Domain: https://dev-xxxxxxx.okta.com
    To set your password open this link:
    https://dev-xxxxxxx.okta.com/welcome/xrqyNKPCZcvxL1ouKUoh
    ```
    e.打开这个链接设置你的密码并后跟这些指令,你 的Okta 域名将返回,类似如下:
    ```text
    New Okta Account created!
    Your Okta Domain: https://dev-xxxxxxx.okta.com
    ```
    f. 为你的Okta 域做一个笔记, 讲它使用在此指南出现的任何时候的`${yourOktaDomain}` 进行替换 ..
3. 运行`okta login`  去连接到你的组织(如果你没有在最后一部分创建 - 成功创建一个Okta 组织并登录),你需要你的组织的URL,它是后跟在`https://` 后跟你的Okta 域 以及 一个 [API/access token](https://developer.okta.com/docs/guides/create-an-api-token/).

    > 注意,如果你使用了一个存在的组织,验证 api 访问管理是启用的, 打开admin console, 点击Security>API, 并验证Authorization Servers 标签页是否存在 ..
    > 如果没有, 选择以下任意之一
    > - 使用Okta CLI 创建一个开发者账户和组织
    > - 联系你的支持团队去在组织中启用这个特性
    > - 使用Admin Console 去创建你的应用集成(替代CLI)

    所有由Okta CLI创建的账户都是开发者账户 ..

## 为应用创建一个Okta 集成

一个应用集成表示在Okta 组织中的一个app, 集成配置配置如何让app 与Okta 服务集成包括: 哪些用户以及分组可以访问,认证策略, token 刷新要求,重定向url 以及其他 ..

集成包括了由app 访问Okta 所需要的配置信息 ..

为了使用CLI 在Okta 中创建你的app 集成

1.通过运行以下命令创建app 集成 
```shell
okta apps create web
```
> 如果Okta CLI 返回了错误(你的Okta 组织缺失了一个由Okta CLI 使用的特性: API Access Management), ",
> 你没有使用一个Okta development account,为了解决这个问题,配置 Okta(参考前面的说明)..

2. 在提示输入应用程序名称时输入 Quickstart。
3. 指定需要的重定向URI 值:
   
    - 重定向URL: `http://localhost:8080/login/oauth2/code/okta`
    - 登出重定向URI: `http://localhost:8080`
   
4. Okta CLI 会创建一个`.okta.env` 文件,以及包含了客户端id ,客户端秘钥 以及颁发者的暴露语句.. 保证此文件的安全(当你使用它配置了你的web app之后) ..

此时,你能够开始创建你的应用了, 如果你想要手动配置集成(或者发现CLI 帮你做了什么),继续阅读:
1. 使用管理员账户  [登录到Okta 组织](https://developer.okta.com/login)
2. 点击页面上的Admin 按钮
3. 打开应用配置面板(通过选择Applications > Applications)
4. 点击创建App 集成
5. 选择OIDC-OpenID Connect的登录方式,然后点击下一步
6. 选择一个Web应用的应用类型,然后点击下一步
    
    > 注意：如果你选择了一个不合适的应用程序类型，它可能会破坏签入或签出流程，因为它需要验证客户的秘密，而这是公共客户没有的东西。
7. 输入app 集成名称
8. 输入登录重定向的urI(对于本地开发),例如`http://localhost:xxx/authorization-code/callback` 
9. 输入登出重定向uri(对于本地开发),例如`http://localhost:xxx/signout/callout`,有关回调uri的更多信息,查看 [定义回调路由](https://developer.okta.com/docs/guides/sign-into-web-app-redirect/spring-boot/main/#define-a-callback-route) ..
10. 在分配部分,定义你的应用的控制访问类型,选择 `everyone group for now`,有关更多信息,了解[分配应用集成](https://help.okta.com/okta_help.htm?type=oie&id=ext-lcm-user-app-assign) 在Okta 产品文档上的主题 ..
11. 点击保存创建应用集成,在保存之后集成的配置页面将会打开 ..  保持此页面打开,因为在你配置应用的时候你需要复制一些值 ..


## 创建应用
使用Okta Spring Boot 启动器或者Spring 初始化器去创建一个示例app ..

1. 导航到 [Spring Initializer](https://start.spring.io/) 去选择Spring Web 以及 Okta 作为依赖,你能够使用[此链接](https://start.spring.io/#!type=maven-project&language=java&packaging=jar&jvmVersion=11&groupId=com.example&artifactId=demo&name=demo&description=Demo%20project%20for%20Spring%20Boot&packageName=com.example.demo&dependencies=web,okta)自动完成这些东西 ..
2. 点击生成
3. 解压下载的demo 到合适的地方准备开始测试 ..

> 此指南需要spring boot 2.6
> 如果你使用Okta cli, 你可以运行  `okta start spring boot ` 去创建 app, 这个命令将会在Okta 中创建一个OIDC app,下载`[okta-spring-boot-sample](https://github.com/okta-samples/okta-spring-boot-sample)` 示例 ..
> 然后配置它和OIDC app 一起工作,这个快速开始使用基本的starter app 替代,因为更容易理解 Okta 特定的添加物(如果你自己通过它们工作 或者研究)

### 增加包
增加需要的包(对应在web app中使用的Okta SDK) ..
如果需要,增加必要的依赖,取决于你是maven 还是 gradle . 注意到这个app(如果通过spring 初始化器创建的已经包含了正确 的依赖)
#### maven
```xml
<dependency>
  <groupId>com.okta.spring</groupId>
  <artifactId>okta-spring-boot-starter</artifactId>
  <version>2.1.4</version>
</dependency>
```
#### gradle
```text
implementation group: 'com.okta.spring', name: 'okta-spring-boot-starter', version: '2.1.4'
```

### 配置你的应用
我们的应用使用来自Okta集成的信息(早期创建来和API交流的配置),客户端ID,客户端秘钥,以及颁发者 ..

如果你使用Okta CLI 创建你的okta app 集成,它将创建一个`.okta.env` 文件到你的目录中(且包含这些值),例如:
```text
export OKTA_OAUTH2_ISSUER=https://${yourOktaDomain}/oauth2/${authorizationServerId}
export OKTA_OAUTH2_CLIENT_ID=${clientId}
export OKTA_OAUTH2_CLIENT_SECRET=${clientSecret}
```

运行`source .okta.env` 在终端窗口中将它们设置为环境变量, 如果你使用`windows` ,你能够改变`export` 作为` set` , 重命名文件为`okta.bat` 然后执行它..

如果你使用`okta start spring-boot` 创建一个应用,它有一个`.okta.env` 文件(稍微看起来有一点不同),这是因为它配置去使用`[spring-dotenv](https://github.com/paulschwarz/spring-dotenv)` 去从此文件中加载配置i

> 注意通过其他方式配置这些属性,可以查看 [spring boot 扩展配置](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)

### 发现你的配置值
如果你不能很容易的处理配置值, 你能够在Admin Console中发现他们 ..
- client ID: 客户端id
- Client Secret: 客户端的凭证
- Okta Domain: 你能够发现在dashboard的右上角的全局header(标题)中 .. 点击电子邮件地址旁边的下拉箭头 然后出现在下拉框中,将指针移动到域名上,单击出现的复制到剪切板图标 .. 复制该域名 ..
    
    > 注意: 你的Okta 域是不同于你的管理域的,你的Okta 域没有包括`-admin` ,例如 `https://dev-133337.okta.com` ..
  
### 重定向到登录页面

为了认证一个用户,你的web app 需要重定向浏览器到 Okta 托管的登录页面 .. 这通常发生在一个登录动作,例如点击按钮或者当用户参观一个受保护的页面 ..

默认情况下,重定向到登录页面会自动的发生(当用户访问一个受保护的路由,默认情况下,Spring Security保护所有路由) ..

让用户点击登录按钮或者链接 - 使用你偏爱的任何模版语言(例如[示例](https://github.com/okta/samples-java-spring/tree/master/okta-hosted-login)中使用thymeleaf),这个链接必须导航到`/oauth2/authorization/okta` ..

```text
<a href="/oauth2/authorization/okta">Sign In</a>
```
在成功的认证Okta 重定向到app(且包含了一个授权码 - 用来交换一个Id 以及一个可以用来询问登录状态的访问token),
之后可以在app中查看[某些返回的用户信息](https://developer.okta.com/docs/guides/sign-into-web-app-redirect/spring-boot/main/#get-info-about-the-user) ..

> 为了定制 Okta 登录表格,你能够查看 [OKta 托管的登录弹窗的风格](https://developer.okta.com/docs/guides/custom-widget/main/#style-the-okta-hosted-sign-in-widget)

### 定义回调路由

确保你的回调路由指定正确, Okta Spring Boot Starter 利用了Spring Security 并且默认是`/login/oauth2/code/okta` 的回调路由.. 这也是在你的Okta app 集成中指定的配置 ..

> 注意 [spring boot sample apps](https://github.com/okta/samples-java-spring) 使用的是`/authorization-code/callback` 


### 获取有关用户的信息

在用户登录之后,Okta 返回用户的某方面个人信息 给app,例如展示在 `/userInfo 响应的示例`中的内容 ..

这些 信息的使用之一是更新你的用户接口,例如展示客户的名称 ..

默认的profile 项(叫做 `claims` ) 由 Okta 返回的包括 用户的邮箱地址,名称,偏好的用户名 .. 这些`claims`  取决于你的应用所请求的scope而也许不同 .

查看[配置你的app](#配置你的应用)

例如当你登录成功之后,下面的代码则返回用户的名称:
```java
@GetMapping("/")
public String hello(@AuthenticationPrincipal OidcUser user) {
  return "Hello, " + user.getName();
}

```
详细示例,查看 [spring boot sample code](https://github.com/okta-samples/okta-spring-boot-sample/blob/main/src/main/java/com/example/sample/Application.java#L17) ..

## 登录一个用户

我们可以启动你的服务器并登录一个用户来测试集成

1. 运行以下命令启动app

```shell
mvn spring-boot:run
```

2. 打开浏览器导航到`http://localhost:8080` . 你能够重定向到Okta 去登录,当你返回的时候,它应该能够展示你的用户信息 ..

> 如果你没有任何控制器映射到`/`,那么你可能会在一个spring boot app认证之后得到 404,在登录之后你可以通过增加合适的控制器去显示用户的名称(例如[获取有关用户信息部分](#获取有关用户的信息)的说明)

## 配置需要的认证
你能够让你的app 对整个站点都需要认证 或者仅仅对指定的路由进行认证 .. 不需要认证的路由不需要登录即可访问,这叫做匿名访问 ..

### 对一切事情 required authentication
```java
@EnableWebSecurity
public class SecurityConfiguration {

  @Bean
  SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeRequests((requests) -> requests.anyRequest().authenticated());

    // enables OAuth redirect login
    http.oauth2Login();

    // enables OAuth Client configuration
    http.oauth2Client();

    // enables REST API support for JWT bearer tokens
    http.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);

    return http.build();
  }
}

```

### 对特定的路由 required authentication
```java
http
  .authorizeRequests(request ->
    request.antMatchers("/checkout/**").authenticated())
```
或者允许特定的分组去访问站点的一部分
```java
http
  // ...
  .authorizeHttpRequests(authorize -> authorize
    .mvcMatchers("/admin/**").hasAuthority("Admin Group")
```
你需要配置一个[`groups` claims](https://developer.okta.com/docs/guides/customize-tokens-groups-claim/main/#add-a-groups-claim-for-the-org-authorization-server) 到授权服务器来让它工作

### Allow anonymous access

能够匿名访问某些路由,但是其他路由需要登录或者做其他的动作,例如 一个电子商务的站点可能允许一个用户匿名浏览并将一些东西加入到购物车,但是需要用户登录来检查(确认)和支持 ..

你能够允许在特定路由上的匿名访问(通过spring security的`permitAll` 到配置中)
```java
http
  // ...
  .authorizeHttpRequests(authorize -> authorize
    .mvcMatchers("/", "/about").permitAll()
```

## 总结
这是一个针对oauth2的 okta 登录的解决方案 ..

## 下一步
- [保护api 端点](https://developer.okta.com/docs/guides/protect-your-api/)
- [自定义域以及邮箱地址](https://developer.okta.com/docs/guides/custom-url-domain/main/)
- [Okta 托管的登录窗口](https://developer.okta.com/docs/guides/custom-widget/main/#style-the-okta-hosted-sign-in-widget)
- [使用重定向模型 登录用户到移动app上](https://developer.okta.com/docs/guides/sign-into-mobile-app-redirect/)
- [多租户解决方案](https://developer.okta.com/docs/concepts/multi-tenancy/)
- [okta 开发者博客日志](https://github.com/oktadev?q=spring-boot&type=all&language=&sort=stargazers)










