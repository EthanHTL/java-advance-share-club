# spring security 和 okta saml2的最佳实践

## 关键点

1. SP Entity ID

    它将会放置在SAML 认证请求响应中 .. 也就是说,它其实就是 认证请求响应中的<ISSUER>
    此Entity ID 参考 spring security对应的 依赖方注入类(Saml2RelyingPartyProperties) 中包含了相关的说明 ..
2. 那么SP ISSUER
    自然就是 SP Entity ID
3. 其次 断言消费服务端点(ACE - assertion consumer service endpoint)
    它本质上表示消费认证响应 / 或者断言的 端点,那么 在okta中,保持它和 接受者url 以及 目的地 url 一致即可 ..

4. SP 公钥证书(用来验证 授权请求或者断言等相关的签名)
5. AP(断言方) 公钥(被授权方用来验证 响应签名等相关配置) ..


6. 具体对应的配置文档参考 [https://help.okta.com/en-us/Content/Topics/Apps/Apps_App_Integration_Wizard_SAML.htm]

7. 在Okta中配置的属性语句可以表示和应用共享的属性

其他的没有什么难的 ,对于spring boot 配置来说,一切都做完了 ..  如果你需要深度了解 内部原理,则参考官方文档 ..

