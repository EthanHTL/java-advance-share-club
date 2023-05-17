# saml 2.0 login

saml 2.0登录特性提供了让一个应用有能力表现为saml 2.0 依赖方, 让用户通过他们在saml 2.0 断言方(Okta,ADFS,以及其他)使用现有账户登录应用程序 ..

> saml 2.0 登录通过使用 web 浏览器 sso profile实现, [SAML 2 Profiles](https://www.oasis-open.org/committees/download.php/35389/sstc-saml-profiles-errata-2.0-wd-06-diff.pdf#page=15) 中指出

从2009年开始,对依赖方的支持已经作为一个扩展项目存在,在2019年,此流程开始移植到 spring security中 ..

这个流程类似从2017年开始的 spring security对OAuth2.0 的支持 ..

> 对于saml 2.0 登录的工作示例 在 spring security samples repository 可用 ..

