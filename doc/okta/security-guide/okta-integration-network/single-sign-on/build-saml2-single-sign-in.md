# 构建SAML2单点登录

## 学习结果
- 在Okta中创建一个app 集成去使用 Okta 作为你应用的身份提供者
- 测试app 集成

## 需要知道的是
- OKta 开发者账户
- 创建一个和Okta集成的app

## 概述
座位一个应用开发者,我们想要让用户能够直接使用Okta 登录到你的应用中(来进行身份管理) .. 为了这样做,你的应用需要支持联合单点登录(SSO),
在这种场景下,你的应用依赖Okta 去担任(作为)一个外部的身份提供者(IDP) ..

Okta 支持OIDC 以及 SAML 2.0 协议去实现单点登录(来进行app 集成)

## 组织
在通常情况下,你的应用依赖于Okta 去表现为一个多租户的身份提供者(针对顾客的Okta 组织) ..

一个OKta 组织表现为一个容器(能够为所有用户,应用以及和单个客户关联的其他实体设置硬边界, 提供基于租户的隔离) ..

在开发你自己的SSO 应用集成的时候, 顾客的Okta 组织充当 授权服务器(OIDC) 或者 IDP(SAML) 身份提供者 ..

## 发布
这个指南假设你有意的开发一个app 集成并且使他通过发布在 Okta 集成网络上(OIN -> okta integration network)公开, 如果你想要开发一个自定义的app 集成(有意的私有化部署到自己的公司内部),
需要使用[App 集成指南(向导) - ALW](https://help.okta.com/okta_help.htm?id=ext_Apps_App_Integration_Wizard)  去创建你app 的集成 ..


## 准备集成
当你决定好使用哪一种协议的时候,你需要收集相关集成的一些信息 ..

## 准备好一个SAML 集成
在saml 集成中, okta 是一个身份提供者, 并且你的应用是一个服务提供者(SP),如果你需要协议背后更多东西或者了解应用的SAML最佳实践 ..

查看 [SAML 概念](https://developer.okta.com/docs/concepts/saml/) 文档

在Okta 创建一个新的SAML 集成之前:
1. 为你的应用确定默认的断言消费者服务(ACS assertion Consumer Service)的URL,这通常称为 SP 登录 URL ..
这是一个当SAML 响应发送到你应用的一个目的地端点 ..
2. 发现你的受众URI,有时候也称为SP Entity ID 或者 应用的 Entity ID(就是指的是你应用的Uri,或者说域,也表示这个IDP的受众)
3. 可选的,配置默认的中继状态页面, 当用户成功通过SAML登录到SP之后的一个用户着陆点(这必须是一个有效的URL) ..
4. 收集任何需要的SAML 属性,你能够选择去共享Okta 用户的个人信息(profile 字段的值) 作为SAML 属性与应用程序共享 ..

注意: SAML 集成必须使用SHA256 编码为了安全 .. 如果你使用SHA-1 来进行编码, 查看指南怎样去升级[SAML APP到 SHA256](https://developer.okta.com/docs/guides/updating-saml-cert/) ..

## 创建你的集成
在你了解背景信息之后,你能够使用Okta Admin 控制台并通过 应用集成向导(AIW)去创建你的SSO 集成(通过关联到你开发者账户的Okta 组织内)

> 注意: 创建你的SSO app 集成并没有在OIN 上可用, 在你创建并测试之后,你可以[提交集成](https://developer.okta.com/docs/guides/submit-app/)到OIN ..

1. 登录Okta 开发者账户(作为一个具有管理员权限的用户)
2. 在管理控制台上,创建app 集成(Applications > Applications)
3. 点击 Create App Integration

### 创建SAML 集成
1. 选择登录方式

    选择: SAML 2.0
2. 点击下一步
3. 在通用设置标签页中, 输入集成的名称以及可选的logo,你能够选择从终端用户的Okta dashboard或者移动app中隐藏这个集成 .. 点击下一步 
4. 在配置SAML的标签页中,使用SAML 信息(在预备阶段收集的信息)来配置集成的设置 .. 查看 [使用集成指南创建SAML 集成](https://help.okta.com/okta_help.htm?id=ext_Apps_App_Integration_Wizard-saml) ..
   
    1. 在登录URL(single sign on URL)上设置 断言消费者服务URL(ACS)
    2. 输入Audience URI 到 Audience URI(SP Entity ID) 字段
       > 注意:  如果你仅仅使用示例SAML  SP 测试你的配置(例如[在github上的saml service provider](https://github.com/mcguinness/saml-sp)),
       > 输入以下的URL到Single sign on URL 以及 Audience URL(SP Entity ID) 字段:
       > `http://example.com/saml/sso/example-okta-com`
    3. 选择Name ID format 以及 应用 用户名称 这些必须发送到传输给你的应用的SAML 响应中(例如 EmailAddress / Email) 或者留默认值 ..
    4. 在属性语句(可选部分), 填入需要和应用共享的一些属性(SAML 属性):
        - FirstName      `user.firstName`
        - LastName       `user.lastName`
        - Email          `user.email`
    5. 如果你使用分组去分类用户, 填充`Group Attribute Statements(可选)` 部分去填充 在SAML 断言中的分组成员关系 .
       1. Name - `groups`
       2. Filter - `Matches regex`
       3. Value - `.*`
       
    6. 你能够预览生成的SAML 断言 - 通过点击预览SAML 断言
    7. 点击下一步
    8. 最终的创建步骤,Feedback 标签页能够帮助Okta 去理解你想要的应用定位 ..
       1. 如果你仅仅创建一个内部的SAML 集成
            1. 选择 `I'm an Okta customer adding an internal app`
            2. 对于出现的复选框,选择 app 类型的选择框(如果你的公司创建的集成并且不希望公开发布),点击这个box,不需要填入额外的信息..
            3. 选择`Contact app vendor` 单选框(如果Okta 需要和你约定去为这个集成启动SAML),如果你点击了这个单选框,那么你需要提供更多有关集成到Okta OIN 团队的更多信息 ..
            4. 点击完成
    9. 对于为 OIN 创建 SAML 集成的 ISV：
       1. 选择 'I'm a software vendor, I'd like to integrate my app with Okta' ..
       2. 点击完成

## 指定你的集成配置
这一部分指南让你能够通过下面的步骤配置你特定的SSO 集成(使用Okta Admin 控制台)

在创建你的集成步骤中创建了集成之后,管理控制台会打开你新集成的主要配置页面 .. 在这里你能够指定通用配置 以及 登录选项 ..
同样也能够分配集成到你组织的用户 .. 点击Edit（如果你需要改变任何选项),点击保存- 做出改变 .

### 指定SAML 配置
1. 在通用标签页中,在应用区域, 你能够重命名集成 并选择可见性以及 启动选项,你能够为SAML 设置做出任何改变(如果你想要改变这些原始值) ..
2. 在登录标签页,你能够下载对于你的集成的 Identity Provider 元数据, 这个信息将需要用来配置SAML 链接配置(在你的SAML SP 应用中):
   1. 登录方法部分,找到身份提供者元数据链接，就在CREDENTIALS DETAILS部分上面。
   2. 右击 Identity Provider metadata 链接并选择复制链接Address .. 这个元数据 包含在这个链接中 包含了你的SAML SP应用需要的信息 ..
    
        我们推荐复制Identity Provider 元数据连接去动态的配置元数据, 如果你的SP 不支持 动态配置,点击链接并查看你所需要的配置信息吧 ..
   
      1. Identity Provider Issuer
      2. X.509 Certificate
      3. Identity Provider Single Sign-on URL
   3. 在SAML SP 应用中, 你能够粘贴这个链接或者所需要的元数据去配置 IDP 元数据 ..

## 使用 SAML 工具包
如果你想要对已有的应用增加SAML SSO, 以下包含了一些开源的 以及 付费工具包 能够帮助你实现SAML 2.0规范 针对服务提供者的WebSSO 配置提供了不同语言的实现:
- .NET Framework 4.5 or later: [Sustainsys.Saml2](https://github.com/Sustainsys/Saml2)(formerly Kentor Authentication Services)
- .NET Framework 4.0 or earlier: [ComponentSpace SAML 2.0 for ASP.NET and ASP.NET Core- Paid software](https://www.componentspace.com/), single developer licenses start at $99
- Java: OpenSAML, which is part of the [Shibboleth Development Project](https://www.shibboleth.net/)
- Java: [Spring Security SAML](https://developer.okta.com/code/java/spring_security_saml)
- PHP: [SimpleSAMLphp](https://developer.okta.com/code/php/simplesamlphp)
- Python: [PySAML2](https://developer.okta.com/code/python/pysaml2)
- Ruby: [Ruby-SAML](https://github.com/onelogin/ruby-saml)

> Okta 没有自己的或者维护这些工具, 然而我们提供这些文档来帮助你与OKta 使用..


## 测试你的集成
这一部分指南让你能够通过以下步骤来测试你的集成

### 分配用户

首先你必须分配你的集成到在你的组织中的一个或者多个测试用户

1. 点击分配选项卡
2. 点击分配 并选择要么分配用户或者分配到组
3. 输入合适的用户或者分组 - 你想要让他们能够单点登录到你的应用,为每一个点击分配 ..
4. 对于每一个增加的用户,验证用户特定的属性, 然后选择保存并返回 ..
5. 点击Done


### 测试单点登录
1. 登出你的开发者组织的管理员账户
2. 登录到Okta 终端用户的dashboard 作为一个分配到这个集成的一个常规用户 ..
3. 在您的仪表板中，单击集成的 Okta 磁贴并确认用户已登录到您的应用程序。

## SAML 疑问诊断

如果你在登录过程中存在问题,能够尝试以下步骤诊断问题:
1. 使用[Okta SAML validation tool](http://saml.oktadev.com/) 去加速开发SAML SP的过程 。。
    这个工具让你能够容易的发送SAML 请求到 你的SAML SP中 .. 它允许你快速的改变SAML请求的内容并简化SAML 疑问的诊断过程 - 通过
2. 自动的解码SAML 负载并为你展示服务器的headers..
3. 你能够安装 [SAML Tracer extension to Firefox](https://addons.mozilla.org/en-US/firefox/addon/saml-tracer/) 来测试, 或者类似的对于其他浏览器的插件
4. 发送问题到[Okta 开发者 论坛](https://devforum.okta.com/search?q=saml) 或者在 [Stack Overflow](https://stackoverflow.com/search?q=saml+okta)上发布 ..


## 下一步
- 在你完成了测试并且你的集成如期工作之后,你能够开始提交流程去让你的集成包括在[Okta 集成网络中] 目录中(https://www.okta.com/okta-integration-network/)
- 提交应用集成的指南让你能够通过这些步骤确保提交你的SSO 集成(通过OIN 管理器) ..

## 总结
OIN 没有必要 ..(这样它将能够被所有人访问, 也就是公开的示例而已 ..)

## see also

- [Okta SAML FAQs](https://developer.okta.com/docs/concepts/saml/faqs/)
- [Okta Developer Forum - OIDC](https://devforum.okta.com/search?q=oidc)
- [Stack Overflow - Okta OIDC](https://stackoverflow.com/search?q=oidc+okta)
- [Okta Developer Form -SAML](https://devforum.okta.com/search?q=saml)
- [Stack Overflow - Okta SAML](https://stackoverflow.com/search?q=saml+okta)

















