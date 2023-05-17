# 认证 <saml2:Response>s

为了验证SAML 2.0 响应, spring security 使用  Saml2AuthenticationTokenConverter 去填充Authentication 请求并 通过 OpenSaml4AuthenticationProvider 去认证它 ..

你能够配置它(以各种方式):
1. 改变查询RelyingPartyRegistration的方式
2. 设置一个时钟偏移 来进行时间戳验证
3. 映射响应到一组 GrantedAuthority 实例 ..
4. 自定义 解码响应和断言元素的策略

为了配置这些,你能够使用 dsl中的 `saml2Login#authenticationManager` 方法 ...

## 改变 RelyingPartyRegistrationLookup
RelyingPartyRegistration 查询是通过 RelyingPartyRegistrationResolver 自定义 ..

当处理<saml2:Response> 负载时为了应用了一个RelyingPartyRegistrationResolver, 你应该首先发布一个 `Saml2AuthenticationTokenConverter` bean 看起来像:
```java
@Bean
Saml2AuthenticationTokenConverter authenticationConverter(InMemoryRelyingPartyRegistrationRepository registrations) {
	return new Saml2AuthenticationTokenConverter(new MyRelyingPartyRegistrationResolver(registrations));
}
```
重新配置默认的断言消费者服务URL`/saml2/login/sso/{registrationid}`, 如果你不在想要url中的`registrationId` ,那么 在过滤链中 以及你的 依赖方的元数据 改变它 ..
```java
@Bean
SecurityFilterChain securityFilters(HttpSecurity http) throws Exception {
	http
        // ...
        .saml2Login((saml2) -> saml2.filterProcessingUrl("/saml2/login/sso"))
        // ...

    return http.build();
}
```
然后在RelyingPartyRegistration中设置
```java
relyingPartyRegistrationBuilder.assertionConsumerServiceLocation("/saml2/login/sso")
```

## 配置时钟偏移

这是不常见的(断言方和依赖方有系统时钟 没有完全同步的情况很少), 对此,你能够配置默认的OpenSaml4AuthenticationProvider的断言验证器 ..并具有一定的容忍度 ..
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        OpenSaml4AuthenticationProvider authenticationProvider = new OpenSaml4AuthenticationProvider();
        authenticationProvider.setAssertionValidator(OpenSaml4AuthenticationProvider
                .createDefaultAssertionValidator(assertionToken -> {
                    Map<String, Object> params = new HashMap<>();
                    params.put(CLOCK_SKEW, Duration.ofMinutes(10).toMillis());
                    // ... other validation parameters
                    return new ValidationContext(params);
                })
        );

        http
            .authorizeHttpRequests(authz -> authz
                .anyRequest().authenticated()
            )
            .saml2Login(saml2 -> saml2
                .authenticationManager(new ProviderManager(authenticationProvider))
            );
        return http.build();
    }
}
```

## 与UserDetailsService 协调
或者,你可能想包含来自遗留的`UserDetailsService` 的用户详情, 这种情况下,响应的认证转换器迟早有用 .. 正如下面的示例:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        OpenSaml4AuthenticationProvider authenticationProvider = new OpenSaml4AuthenticationProvider();
        authenticationProvider.setResponseAuthenticationConverter(responseToken -> {
            Saml2Authentication authentication = OpenSaml4AuthenticationProvider
                    .createDefaultResponseAuthenticationConverter()
                    .convert(responseToken);
            Assertion assertion = responseToken.getResponse().getAssertions().get(0);
            String username = assertion.getSubject().getNameID().getValue();
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            return MySaml2Authentication(userDetails, authentication);
        });

        http
            .authorizeHttpRequests(authz -> authz
                .anyRequest().authenticated()
            )
            .saml2Login(saml2 -> saml2
                .authenticationManager(new ProviderManager(authenticationProvider))
            );
        return http.build();
    }
}
```
1. 首先,调用默认的转换器, 这将从响应中抓取属性以及授权信息 ..
2. 第二,使用相关信息调用UserDetailsService
3. 第三方,返回包含用户详情的自定义认证

> 注意:
> 它不需要调用 OpenSaml4AuthenticationProvider 的默认认证转换器 ..
> 它返回一个Saml2AuthenticatedPrincipal(包含了从AttributeStatement中抓取的属性) 以及 单个 `ROLE_USER` 权限 ..

## 执行额外的响应验证
`OpenSaml4AuthenticationProvider` 验证了Issuer 以及 Destination 值(在立即解码了Response) .. 
你能够自定义验证(通过扩展默认的验证器 - 通过关联你自己的响应验证器),或者你能够完全的替代它 ..

举个例子,你能够抛出一个自定义异常(使用Response 对象中可用的额外信息),就像:
```java
OpenSaml4AuthenticationProvider provider = new OpenSaml4AuthenticationProvider();
provider.setResponseValidator((responseToken) -> {
	Saml2ResponseValidatorResult result = OpenSamlAuthenticationProvider
		.createDefaultResponseValidator()
		.convert(responseToken)
		.concat(myCustomValidator.convert(responseToken));
	if (!result.getErrors().isEmpty()) {
		String inResponseTo = responseToken.getInResponseTo();
		throw new CustomSaml2AuthenticationException(result, inResponseTo);
	}
	return result;
});
```

## 执行额外的断言验证
`OpenSaml4AuthenticationProvider` 在SAML 2.0 断言上最小化验证, 在验证签名之后, 它将:
1. 验证 <AudienceRestriction> 以及 <DelegationRestriction> 条件
2. 验证 <SubjectConfirmation>, 期待任何IP 地址信息

为了执行额外的验证,你可以配置自己的断言验证器 代理到 `OpenSaml4AuthenticationProvider`的默认断言验证器 然后并执行自己的 ..断言验证器 ..

例如,你能够使用OpenSAML的 OneTimeUseConditionValidator 也去验证`<OneTimeUse>` 条件 ..
```java
OpenSaml4AuthenticationProvider provider = new OpenSaml4AuthenticationProvider();
OneTimeUseConditionValidator validator = ...;
provider.setAssertionValidator(assertionToken -> {
    Saml2ResponseValidatorResult result = OpenSaml4AuthenticationProvider
            .createDefaultAssertionValidator()
            .convert(assertionToken);
    Assertion assertion = assertionToken.getAssertion();
    OneTimeUse oneTimeUse = assertion.getConditions().getOneTimeUse();
    ValidationContext context = new ValidationContext();
    try {
        if (validator.validate(oneTimeUse, assertion, context) = ValidationResult.VALID) {
            return result;
        }
    } catch (Exception e) {
        return result.concat(new Saml2Error(INVALID_ASSERTION, e.getMessage()));
    }
    return result.concat(new Saml2Error(INVALID_ASSERTION, context.getValidationFailureMessage()));
});
```
> 虽然推荐, 它没有必要去调用 OpenSaml4AuthenticationProvider的默认断言验证器 .. 一个环境(当 你不需要去检测<AudienceRestriction> 或者 <SubjectConfirmation>的情况下你需要跳过,因为这些你可以自己做) ..

## 自定义解密
spring security 解码<saml2:EncryptedAssertion>,<saml2:EncryptedAttribute> 以及 <saml2:EncryptedId> 元素 - 自动的通过使用注册在RelyingPartyRegistration中的 [解密 Saml2X509Credential 实例](https://docs.spring.io/spring-security/reference/servlet/saml2/login/overview.html#servlet-saml2login-rpr-credentials) ..

OpenSaml4AuthenticationProvider 暴露了 两种解密策略 .. 响应解码器是负责解码<saml2:Response>的加密元素 ..
例如 <saml2:encryptedAssertion>.. 断言解码器负责解密 <saml2:Assertion>的加密元素  ..
例如<saml2:EncryptedAttribute> 以及 <saml2:EncryptedID>

你能够替换 OpenSaml4AuthenticationProvider 的默认解密策略(通过自己的实现).. 例如,如果你有一个单独的服务(解密<saml2:Response>的断言),你能够使用它来替代:
```java
MyDecryptionService decryptionService = ...;
OpenSaml4AuthenticationProvider provider = new OpenSaml4AuthenticationProvider();
provider.setResponseElementsDecrypter((responseToken) -> decryptionService.decrypt(responseToken.getResponse()));
```
如果你也想要解密在<saml2:Assertion>中的其他元素 .. 你能够自定义断言解密器:
```java
provider.setAssertionElementsDecrypter((assertionToken) -> decryptionService.decrypt(assertionToken.getAssertion()));
```
> 注意到:
> 这是两个独立的解密器 - 因为断言能够独立的在响应中签名, 尝试解密一个签名的断言的元素(在签名验证之前)可能会无效签名 .. 如果你的断言方仅仅签名了响应,那么它
> 会安全的解密所有使用的元素(使用仅有的响应解密器)

## 使用 一个自定义的Authentication Manager
当然, authenticationManager dsl 方法也能够被用来执行 完整的 SAML2.0 认证 .. 这个认证管理器应该期待
Saml2AuthenticationToken 对象包含 SAML 2.0 响应 xml 数据 ..
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = new MySaml2AuthenticationManager(...);
        http
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated()
            )
            .saml2Login(saml2 -> saml2
                .authenticationManager(authenticationManager)
            )
        ;
        return http.build();
    }
}
```

## 使用Saml2AuthenticatedPrincipal
使用为给定的断言方配置的正确的依赖方, 它已经准备好接受断言 .. 一旦依赖方验证了一个断言 ..
那么这个结果就是 具有Saml2AuthenticatedPrincipal的 SamlAuthentication ..

这意味着你能够访问principal(在控制器中)
```java
@Controller
public class MainController {
	@GetMapping("/")
	public String index(@AuthenticationPrincipal Saml2AuthenticatedPrincipal principal, Model model) {
		String email = principal.getFirstAttribute("email");
		model.setAttribute("email", email);
		return "index";
	}
}
```

> 因为SAML2.0 规范 允许每一个属性都有多个值, 你能够要么调用getAttribute 去获得一组属性,或者`getFirstAttribute` 去获取列表中的第一个 ..
> `getFirstAttribute` 是一个相对便利的方式(如果你知道某个属性仅仅只有一个值) ..










