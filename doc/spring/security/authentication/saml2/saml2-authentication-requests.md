# 产生<saml2:AuthnRequest>s

从早期开始,spring security saml 2.0 支持 产生 <saml2:authnRequest> 去着手 和断言方的认证交互 ..

spring security 实现此部分 - 通过注册一个Saml2WebSsoAuthenticationRequestFilter 到过滤链中 ..

这个过滤器默认响应 /saml2/authenticate/{registrationId}

例如,如果你部署到https://rp.example.com/ 并且 你有一个registration的id 为`okta` ,你能够导航到:
`rp.example.org/saml2/authenticate/okta`

并且最终将会重定向且包含一个SAMLRequest(此参数包含了签名 /压缩 且编码的 <saml2:Auth2Request> )

## 改变<saml2:AuthnRequest> 存储
Saml2WebSsoAuthenticationRequestFilter 使用一个 Saml2AuthenticationRequestRepository 去持久化(存储)一个
AbstractSaml2AuthenticationRequest 实例(在发送<saml2:AuthnRequest> 到断言方之前) ..

除此之外,Saml2WebSsoAuthenticationFilter 以及 Saml2AuthenticationTokenConverter 使用一个 
Saml2AuthenticationRequestRepository 去加载 任何AbstractSaml2AuthenticationRequest 作为 [认证<saml2:Response>](https://docs.spring.io/spring-security/reference/servlet/saml2/login/authentication.html#servlet-saml2login-authenticate-responses) 的一部分 ..

默认情况,spring security 使用一个 HttpSessionSaml2AuthenticationRequestRepository,它存储了AbstractSaml2AuthenticationRequest到HttpSession中 ..

如果你有自定义的Saml2AuthenticationRequestRepository 实现,你也许配置它(通过配置成一个 @Bean),如下示例展示:
```java
@Bean
Saml2AuthenticationRequestRepository<AbstractSaml2AuthenticationRequest> authenticationRequestRepository() {
	return new CustomSaml2AuthenticationRequestRepository();
}
```

## 改变<saml2:AuthnRequest> 的发送
默认情况,spring security 签名每一个<saml2:AuthnRequest> 并以GET请求发送给断言方 ..

许多断言方不需要签名<saml2:AuthnRequest> ,这是能够自动通过RelyingPartyRegistrations 自动配置的 ..
或者你能够手动提供它,例如:
```java
spring:
  security:
    saml2:
      relyingparty:
        okta:
          identityprovider:
            entity-id: ...
            singlesignon.sign-request: false
```
否则,你将需要指定一个私钥(通过 RelyingPartyRegistration#signingX509Credentials),那样spring security能够在发送之前签名<saml2:AuthnRequest> .

默认情况下,spring security 会通过使用rsa-sha256 签名<saml2:AuthnRequest>, 考虑到某些断言方将需要不同的算法 .. 如同在他们自己元数据中的指示 ..

你能够基于断言方的元数据配置算法(使用RelyingPartyRegistrations) ..

或者,你能够手动提供:
```java
String metadataLocation = "classpath:asserting-party-metadata.xml";
RelyingPartyRegistration relyingPartyRegistration = RelyingPartyRegistrations.fromMetadataLocation(metadataLocation)
        // ...
        .assertingPartyDetails((party) -> party
            // ...
            .signingAlgorithms((sign) -> sign.add(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA512))
        )
        .build();
```

> 上面的快照使用OpenSAML `SignatureConstants` 类去提供算法名称, 但是 那仅仅是为了便利 .. 
> 因为数据类型是 `String` ,你能够直接提供算法的名称 ..

某些断言方 需要<saml2:AuthnRequest> 是Post请求, 这能够通过RelyingPartyRegistrations 自动配置, 或者手动应用它,例如:
```java
RelyingPartyRegistration relyingPartyRegistration = RelyingPartyRegistration.withRegistrationId("okta")
        // ...
        .assertingPartyDetails(party -> party
            // ...
            .singleSignOnServiceBinding(Saml2MessageBinding.POST)
        )
        .build();
```

## 自定义 OpenSAML的AuthnRequest 实例

这里有大量的原因让你想要调整一个AuthnRequest .. 例如,你想要`ForceAuthN` 为 true, 默认 spring security 设置为 false ..

你能够自定义OpenSAML的 AuthnRequest的元素 - 通过 发布 OpenSaml4AuthenticationRequestResolver 为一个@Bean, 例如:
```java
@Bean
Saml2AuthenticationRequestResolver authenticationRequestResolver(RelyingPartyRegistrationRepository registrations) {
    RelyingPartyRegistrationResolver registrationResolver =
            new DefaultRelyingPartyRegistrationResolver(registrations);
    OpenSaml4AuthenticationRequestResolver authenticationRequestResolver =
            new OpenSaml4AuthenticationRequestResolver(registrationResolver);
    authenticationRequestResolver.setAuthnRequestCustomizer((context) -> context
            .getAuthnRequest().setForceAuthn(true));
    return authenticationRequestResolver;
}
```


