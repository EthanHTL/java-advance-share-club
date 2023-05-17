# Authorization
Spring Security 中的高级授权功能是其受欢迎的最引人注目的原因之一 . 无关你怎样去进行认证(是否使用spring security提供的机制  - provider 或者与容器 或者和其他非spring security认证授权集成) ..

授权服务能够在应用中使用 - 以一致且简单的方式使用 ..

在这一部分,将简单的探索AbstractSecurityInterceptor 的不同实现, 这将在第一部分介绍 .. 我们然后将继续探索如何调整授权(通过域访问控制列表的使用) ..

## 章节总结
1. 授权架构
2. 授权 http 请求
3. 授权Http 请求(使用FilterSecurityInterceptor)
4. 基于表达式的访问控制
5. 安全对象实现
6. 方法安全
7. 域对象安全acl
8. 授权事件
