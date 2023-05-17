# 执行单点登出
spring security 携带了对 RP 以及 AP 发起的SAML 2.0 单点登出 ..

简短来说,spring security 支持两种使用情况:
1. RP- 发起:

    表示依赖方
    你的应用程序有一个端点, 当POST请求时,将会退出用户并发送<saml2:LogoutRequest> 到 断言方 .. 在这之后, 断言方将会发送一个<saml2:LogoutResponse> 并
允许你的应用去消费 ..
2. AP- 发起:

    表示断言方
    你的应用有一个端点 将会接受一个saml2:LogoutRequest(来自断言方的),你的应用将完全退出并发送<saml2:LogoutResponse> 到断言方 ..

> 在AP-发起的场景中,你应用在登出之后的任何本地重定向是无意义的. 一旦你的应用发送了<saml2:LogoutResponse>,那么它不在受浏览器控制 ..
但是 退出之后的重定向可以引导用户到一个应用希望它到达的地方 ..
## 单点登录的最小化配置
为了使用spring security saml2.0 单点登录特性 .. 你将需要以下的事情 ..

- 首先,断言方必须支持saml2.0 单点登出
- 第二,断言方应该配置去签名 并POST 发送saml2:LogoutRequest 以及 saml2:LogoutResponse 到你的应用的/logout/saml2/slo 端点 ..
- 第三,你的应用必须有一个PKCS#8 私钥以及 X.509 证书用来签名saml2:LogoutRequest 以及 saml2:LogoutResponse ..

你能够从最初最小化示例开始 并增加以下配置:
```java
@Value("${private.key}") RSAPrivateKey key;
@Value("${public.certificate}") X509Certificate certificate;

@Bean
RelyingPartyRegistrationRepository registrations() {
    Saml2X509Credential credential = Saml2X509Credential.signing(key, certificate);
    RelyingPartyRegistration registration = RelyingPartyRegistrations
            .fromMetadataLocation("https://ap.example.org/metadata")
            .registrationId("id")
            .singleLogoutServiceLocation("{baseUrl}/logout/saml2/slo")
            .signingX509Credentials((signing) -> signing.add(credential))
            .build();
    return new InMemoryRelyingPartyRegistrationRepository(registration);
}

@Bean
SecurityFilterChain web(HttpSecurity http, RelyingPartyRegistrationRepository registrations) throws Exception {
    http
        .authorizeHttpRequests((authorize) -> authorize
            .anyRequest().authenticated()
        )
        .saml2Login(withDefaults())
        .saml2Logout(withDefaults());

    return http.build();
}
```
1. 增加签名key到 RelyingPartyRegistration 实例 或者多个实例 ..
2. 指示应用想要使用SAML SLO 去登出最终用户 ..

## 运行时期待

给定上述配置 任何已经登录的用户能够发送`POST /logout` 到你的应用中去执行RP-initiated SLO,你的应用将做以下的事情:
1. 登出用户并无效会话
2. 使用Saml2LogoutRequestResolver 去创建,签名,序列化一个<saml2:LogoutRequest> - 基于和当前登录用户关联的RelyingPartyRegistration ..
3. 发送重定向或者Post到断言方(基于RelyingPartyRegistration)
4. 反序列化,验证,并处理通过断言方发送的<saml2:LogoutResponse> ..
5. 重定向到任何配置的成功登出端点 ..

同样,你的应用能够参与到AP 发起的登出 - 当断言方发送了一个<saml2:LogoutRequest> 到 `/logout/saml2/slo`
1. 使用`Saml2LogoutRequestHandler` 去反序列化,验证处理由断言方发送的<saml2:LogoutRequest>
2. 登出用户并无效会话
3. 创建,签名,序列化一个<saml2:LogoutResponse> 基于退出用户关联的 RelyingPartyRegistration ..
4. 发送一个重定向或者Post到断言方(基于RelyingPartyRegistration)

> 增加saml2Logout 这个能力到给服务提供者 .. 因为这是一个可选的能力,你需要为每一个独立的RelyingPartyRegistration 开启 ..
> 你能够通过设置RelyingPartyRegistration.Builder#singleLogoutServiceLocation 属性设置 ..

## 配置登出端点
这里有三种行为能够被不同的端点触发

- RP-发起登出:

    这允许一个认证的用户发送POST 并触发登出处理 - 通过发送<saml2:LogoutRequest> 到断言方 ..
- AP-发起登出:

    这允许一个断言方发送<saml2:LogoutRequest> 到应用
- AP 登出响应:

    这允许一个断言方发送<saml2:LogoutResponse> 去响应RP 发起的<saml2:LogoutRequest>

首先通过执行常见的POST /logout 触发登出(当身份的类型是 Saml2AuthenticatedPrincipal)

其次通过发送POST 到/logout/saml2/slo 端点(使用断言方签名的SAMLRequest)

第三,通过使用由断言方签名的SAMLResponse 发送POST请求到 `/logout/saml2/slo`触发

因为用户已经登录或者原始登出请求已知,那么registrationId也是已知的,对此,{registrationId} 并不是url的一部分(默认) ..

此URL 是可以在DSL中定制的 ..

举个例子,如果你迁移你存在的依赖方完全到 Spring Security,你的断言方也许已经指向了一个GET `/SLOService.saml2`,为了减少在配置中对断言方的改变,那么我们可以在dsl中配置过滤器如下:
```java
http
    .saml2Logout((saml2) -> saml2
        .logoutRequest((request) -> request.logoutUrl("/SLOService.saml2"))
        .logoutResponse((response) -> response.logoutUrl("/SLOService.saml2"))
    );
```
你也可以在RelyingPartyRegistration中配置这些端点 ..

## 自定义 <saml2:LogoutRequest> 解析
常见需要在<saml2:LogoutRequest> 中设置其他值(spring security 没有提供的) ..

默认情况,spring security 将会颁发一个<saml2:LogoutRequest> 并提供:
- Destination 属性 - 根据 RelyingPartyRegistration#getAssertingPartyDetails#getSingleLogoutServiceLocation
- ID 属性 - 一个GUID
- Issuer 元素 - 来自 RelyingPartyRegistration#getEntityId
- NameID 元素 - 来自 Authentication#getName

为了增加其他的值,你能够使用代理,例如:
```java
@Bean
Saml2LogoutRequestResolver logoutRequestResolver(RelyingPartyRegistrationRepository registrations) {
	OpenSaml4LogoutRequestResolver logoutRequestResolver =
			new OpenSaml4LogoutRequestResolver(registrations);
	logoutRequestResolver.setParametersConsumer((parameters) -> {
		String name = ((Saml2AuthenticatedPrincipal) parameters.getAuthentication().getPrincipal()).getFirstAttribute("CustomAttribute");
		String format = "urn:oasis:names:tc:SAML:2.0:nameid-format:transient";
		LogoutRequest logoutRequest = parameters.getLogoutRequest();
		NameID nameId = logoutRequest.getNameID();
		nameId.setValue(name);
		nameId.setFormat(format);
	});
	return logoutRequestResolver;
}
```
然后你可以应用你自定义的Saml2LogoutRequestResolver 到 dsl中 ..
```java
http
    .saml2Logout((saml2) -> saml2
        .logoutRequest((request) -> request
            .logoutRequestResolver(this.logoutRequestResolver)
        )
    );
```

## 自定义<saml2:LogoutResponse> 解析
常见是设置非spring security 提供的其他值到<saml2:LogoutResponse>

默认情况,spring security 将颁发一个<saml2:LogoutResponse> 并提供:
- Destination 属性  RelyingPartyRegistration#getAssertingPartyDetails#getSingleLogoutServiceResponseLocation
- ID 属性 - 一个GUID
- \<Issuer\> 元素 - 从 RelyingPartyRegistration#getEntityId
- \<Status\> 元素 - `SUCCESS`

为了增加其他的值,你能够使用代理:
```java
@Bean
public Saml2LogoutResponseResolver logoutResponseResolver(RelyingPartyRegistrationRepository registrations) {
	OpenSaml4LogoutResponseResolver logoutRequestResolver =
			new OpenSaml4LogoutResponseResolver(registrations);
	logoutRequestResolver.setParametersConsumer((parameters) -> {
		if (checkOtherPrevailingConditions(parameters.getRequest())) {
			parameters.getLogoutRequest().getStatus().getStatusCode().setCode(StatusCode.PARTIAL_LOGOUT);
		}
	});
	return logoutRequestResolver;
}
```
然后,你能够提供自定义的Saml2LogoutResponseResolver 在DSL中..
```java
http
    .saml2Logout((saml2) -> saml2
        .logoutRequest((request) -> request
            .logoutRequestResolver(this.logoutRequestResolver)
        )
    );
```

## 定制 <saml2:LogoutRequest> 认证
为了自定义验证,你需要实现你自己的 Saml2LogoutRequestValidator，此时,验证是最小化的,因此你也许能够首先代理到默认的Saml2LogoutRequestValidator:
```java
@Component
public class MyOpenSamlLogoutRequestValidator implements Saml2LogoutRequestValidator {
	private final Saml2LogoutRequestValidator delegate = new OpenSamlLogoutRequestValidator();

	@Override
    public Saml2LogoutRequestValidator logout(Saml2LogoutRequestValidatorParameters parameters) {
		 // verify signature, issuer, destination, and principal name
		Saml2LogoutValidatorResult result = delegate.authenticate(authentication);

		LogoutRequest logoutRequest = // ... parse using OpenSAML
        // perform custom validation
    }
}
```
然后能够提供自定义的Saml2LogoutRequestValidator 到 DSL中:
```java
http
    .saml2Logout((saml2) -> saml2
        .logoutRequest((request) -> request
            .logoutRequestAuthenticator(myOpenSamlLogoutRequestAuthenticator)
        )
    );
```

## 自定义<saml2:LogoutResponse> 认证
同样,需要实现自己的Saml2LogoutResponseValidator. 最好的是代理默认Saml2LogoutResponseValidator 实现最基本的验证.然后自定义自己的验证
```java
@Component
public class MyOpenSamlLogoutResponseValidator implements Saml2LogoutResponseValidator {
	private final Saml2LogoutResponseValidator delegate = new OpenSamlLogoutResponseValidator();

	@Override
    public Saml2LogoutValidatorResult logout(Saml2LogoutResponseValidatorParameters parameters) {
		// verify signature, issuer, destination, and status
		Saml2LogoutValidatorResult result = delegate.authenticate(parameters);

		LogoutResponse logoutResponse = // ... parse using OpenSAML
        // perform custom validation
    }
}
```
然后,你能够提供自己的Saml2LogoutResponseValidator 到DSL中:
```java
http
    .saml2Logout((saml2) -> saml2
        .logoutResponse((response) -> response
            .logoutResponseAuthenticator(myOpenSamlLogoutResponseAuthenticator)
        )
    );
```

## 自定义<saml2:LogoutRequest> 存储
当你的应用发送了一个<saml2:LogoutRequest> 那么这个值是存储在session中的,因此RelayState 参数 以及在<saml2:LogoutResponse> 中的InResponseTo属性能够被验证 ..

如果你想要存储登出请求到某些地方而不是session,你能够提供自己的实现到DSL中 ..
```java
http
    .saml2Logout((saml2) -> saml2
        .logoutRequest((request) -> request
            .logoutRequestRepository(myCustomLogoutRequestRepository)
        )
    );
```


