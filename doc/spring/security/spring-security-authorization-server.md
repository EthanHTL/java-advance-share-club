# spring-security-authorization-server

## 配置模型
### 默认配置
OAuth2AuthorizationServerConfiguration 是一个@Configuration,它提供了OAuth2 授权服务器的最小默认配置 \
当然,我们可以使用 OAuth2AuthorizationServerConfigurer 对默认的配置进行修改 并注册 SecurityFilterChain @Bean \
组合所有支持OAuth2 authorization 服务器的基础设施组件 ... \
OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(HttpSecurity)  是一个方便的工具方法 用来应用默认的OAuth2 安全配置到 HttpSecurity ... \

OAuth2 授权服务器 SecurityFilterChain @Bean 配置了如下默认的协议端点:
- OAuth2 授权端点
- OAuth2 Token 端点
- OAuth2 Token Introspection 端点
- OAuth2 Token Revocation 端点
- OAuth2 Authorization Server 元数据端点
- JWK Set 端点
- OpenID Connect 1.0 Provider 配置端点
- OpenID Connect 1.0 UserInfo 端点 ...

注意:  JWK Set 端点配置仅仅在于 JWKSource<SecurityContext> @Bean 注册之后才会配置 ... \
同样OpenID Connect 1.0 客户端注册端点 默认是禁用的(因为许多部署不需要动态客户端注册) ... \
例如以下是一个默认配置：
```java
@Configuration
@Import(OAuth2AuthorizationServerConfiguration.class)
public class AuthorizationServerConfig {

	@Bean
	public RegisteredClientRepository registeredClientRepository() {
		List<RegisteredClient> registrations = ...
		return new InMemoryRegisteredClientRepository(registrations);
	}

	@Bean
	public JWKSource<SecurityContext> jwkSource() {
		RSAKey rsaKey = ...
		JWKSet jwkSet = new JWKSet(rsaKey);
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}

}
```
授权码授予 需要资源拥有者得到认证, 因此一个用户认证机制 必须配置(除了 OAuth2 security 配置之外) ... \
OAuth2AuthorizationServerConfiguration.jwtDecoder(JWKSource<SecurityContext>) 是一个方便的工具方法能够用来注册一个
JwtDecoder @Bean,它对于OpenID Connect 1.0 用户端点 和 OpenID Connect 1.0 客户端注册端点是必要的 ... \
如何注册一个JwtDecoder @Bean
```java
@Bean
public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
	return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
}
```
OAuth2AuthorizationServerConfiguration 的主要意图是 提供方便的方法去应用OAuth2 授权服务器的最小默认配置,然而,大多数情况下,自定义配置是需要的 ..
### 自定义配置
OAuth2AuthorizationServerConfigurer 能够用来完全配置Security 配置(关于OAuth2 授权服务器的一些配置) ...
它让你指定一些需要使用的核心组件 ,举个例子,RegisteredClientRepository, OAuth2AuthorizationService,OAuth2TokenGenerator 以及其他 ..
因此,它让你能够定制对协议端点的各种请求处理逻辑 .. 举个例子, 授权端点,token 端点, token 检测端点以及其他 ... \
OAuth2AuthorizationServerConfigurer 提供了以下的配置选项:
```java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer =
		new OAuth2AuthorizationServerConfigurer<>();
	http.apply(authorizationServerConfigurer);

	authorizationServerConfigurer
		.registeredClientRepository(registeredClientRepository)  // 管理新的和存在的客户端 ... 这是必须的 ..
		.authorizationService(authorizationService)  // 管理新的和存在的授权
		.authorizationConsentService(authorizationConsentService)    // 管理新的和存在的授权协商 ...
		.providerSettings(providerSettings)  // 必须的, 定制 OAuth2 授权服务器的配置设置..
		.tokenGenerator(tokenGenerator)  // 用来支持OAuth2 授权服务器生成token的 ...
		.clientAuthentication(clientAuthentication -> { })   // Oauth2 客户端认证的配置器 ..
		.authorizationEndpoint(authorizationEndpoint -> { })     // Oauth2 授权端点 ..
		.tokenEndpoint(tokenEndpoint -> { })     // Oauth2 token 端点
		.tokenIntrospectionEndpoint(tokenIntrospectionEndpoint -> { }) // oauth2 token 检测端点  
		.tokenRevocationEndpoint(tokenRevocationEndpoint -> { })    // oauth2 token 撤销端点
		.oidc(oidc -> oidc
			.userInfoEndpoint(userInfoEndpoint -> { })   // openID Connect 1.0 用户端点
			.clientRegistrationEndpoint(clientRegistrationEndpoint -> { })  // OpenID Connect 1.0 客户端注册端点的配置器 ..
		);

	return http.build();
}
```
### 配置提供者配置
ProviderSettings 包含了有关OAuth2 授权服务器(provider)的配置设置 .. 它指定了URI(有关协议端点的URI) 以及 issuer 身份.. 默认的协议端点的URI 如下所示:
```java
public final class ProviderSettings extends AbstractSettings {

	...

	public static Builder builder() {
		return new Builder()
			.authorizationEndpoint("/oauth2/authorize")
			.tokenEndpoint("/oauth2/token")
			.tokenIntrospectionEndpoint("/oauth2/introspect")
			.tokenRevocationEndpoint("/oauth2/revoke")
			.jwkSetEndpoint("/oauth2/jwks")
			.oidcUserInfoEndpoint("/userinfo")
			.oidcClientRegistrationEndpoint("/connect/register");
	}

	...

}
```
并且这是必须的组件 ...,如果你使用@Import(OAuth2AuthorizationServerConfiguration.class),它将自动的注册一个ProviderSettings @Bean 组件 ...(如果你没有提供) ...
ProviderContext 是一个上下文对象,它持有provider的一些信息 ... 它提供了对ProviderSettings和 当前 issuer 身份的访问 ... \
issuer 身份如果没有通过ProviderSettings.builder.issuer(String) 进行配置,那么它将从当前请求中进行解析 ...\
ProviderContext 能够通过ProviderContextHolder进行访问,它将当前请求线程使用ThreadLocal进行关联 ... \
ProviderContextFilter 使用ProviderContextHolder关联一个ProviderContext ...

### 配置 client 认证
这是什么意思?
OAuth2ClientAuthenticationConfigurer provides the ability to customize OAuth2 client authentication, 它定义了扩展点能够让我们自定义预处理,主要处理,后置处理逻辑(关于client 认证请求) ...
OAuth2ClientAuthenticationConfigurer  提供了以下的配置选项:
```java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer =
		new OAuth2AuthorizationServerConfigurer<>();
	http.apply(authorizationServerConfigurer);

	authorizationServerConfigurer
		.clientAuthentication(clientAuthentication ->
			clientAuthentication
				.authenticationConverter(authenticationConverter)    // AuthenticationConverter(预处理器) 被用来尝试抓取客户端凭证(从request) 转换到 OAuth2ClientAuthenticationToken ...
				.authenticationProvider(authenticationProvider)  // AuthenticationProvider (主要处理器) 被用来认证 OAuth2ClientAuthenticationToken(可以增加多个用来执行认证动作) ...详情参考Spring security 基础认证授权机制 ...
				.authenticationSuccessHandler(authenticationSuccessHandler)  // 后置处理器,认证成功之后的收尾处理(关联OAuth2ClientAuthenticationToken 到 SecurityContext) ...
				.errorResponseHandler(errorResponseHandler)  // 后置处理器(当客户端认证失败)并返回 OAuth2Error 响应 ...
		);

	return http.build();
}
```
OAuth2ClientAuthenticationConfigurer 配置 OAuth2ClientAuthenticationFilter  并将它注册到授权服务器的 SecurityFilterChain @Bean中 ... \
OAuth2ClientAuthenticationFilter 是一个过滤器(用来处理客户端认证请求) ... \
默认来说,客户端认证需要 OAuth2 Token端点,OAuth2 Token introspection 端点 以及 OAuth2 Token 撤销端点 ... 支持的客户端认证方法有 client_secret_basic,client_secret_post,
private_key_jwt,client_secret_jwt,以及 none(公有客户端) ... \
OAuth2ClientAuthenticationFilter 配置了默认以下组件:
- AuthenticationConverter 一个DelegatingAuthenticationConverter(它由 JwtClientAssertionAuthenticationConverter ,ClientSecretBasicAuthenticationConverter ,ClientSecretPostAuthenticationConverter, PublicClientAuthenticationConverter)组成
- AuthenticationManager 一个代理AuthenticationManager,它由JwtClientAssertionAuthenticationProvider 和 ClientSecretAuthenticationProvider 以及 PublicClientAuthenticationProvider 提供器组成 ..
- AuthenticationSuccessHandler 后置处理器(内部实现,将Authentication 认证成功的关联到 SecurityContext ...)
- AuthenticationFailureHandler 后置处理器(内部实现,将OAuth2AuthenticationException 到OAuth2Error  并返回OAuth2Error 错误响应...)


## 核心模型 / Components
### RegisteredClient
一个RegisteredClient 表示一个客户端的呈现(它已经注册到授权服务器) ... 一个客户端必须注册到授权服务器上(在它初始化认证授权流之前),例如 authorization_code 或者 client_credentials ... \
在客户端注册期间,客户端会分配一个独一无二的客户端标识,以及一个可选的客户端 secret(依赖于客户端类型),以及和它关联的元数据 ... 这个客户端的元数据能够是人类可以识别的字符串(例如客户端名称)以及特定于它们
协议流的特定数据(例如有效的重定向 URI 列表) .... \
在SpringSecurity的 OAuth2 客户端支持的客户端注册模型是 ClientRegistration ... \
一个客户端的主要目的就是请求访问保护资源, 客户端的第一个请求是为了获取一个访问token(通过授权服务器认证并颁发令牌) .. 授权服务器认证客户端之后(如果它是有效的),则颁发一个访问 token ... 
客户端能够通过这个访问token 从资源服务器中请求访问资源 ...  \
以下例子展示了如何配置RegisteredClient (它允许执行授权码授予流去请求一个访问 token ...)
```java
RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
	.clientId("client-a")
	.clientSecret("{noop}secret")   // 表示PasswordEncoder -> NoOpPasswordEncoder的标识 ...
	.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
	.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
	.redirectUri("http://127.0.0.1:8080/authorized")
	.scope("scope-a")
	.clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
	.build();
```
在Spring Security的 OAuth2 Client 支持的相关配置是:
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          client-a:
            provider: spring
            client-id: client-a
            client-secret: secret
            authorization-grant-type: authorization_code
            redirect-uri: "http://127.0.0.1:8080/authorized"
            scope: scope-a
        provider:
          spring:
            issuer-uri: http://localhost:9000
```
一个已注册的Client具有元数据属性如下:
```java
public class RegisteredClient implements Serializable {
	private String id;  
	private String clientId;    
	private Instant clientIdIssuedAt;   
	private String clientSecret;    
	private Instant clientSecretExpiresAt;   // client secret 过期时间
	private String clientName;  
	private Set<ClientAuthenticationMethod> clientAuthenticationMethods; // 认证方法(客户端使用的),支持的值有很多client_secret_basic, client_secret_post, private_key_jwt, client_secret_jwt, and none (public clients).    
	private Set<AuthorizationGrantType> authorizationGrantTypes;    // 客户端能够使用的授权授予类型 ... 支持的值 authorization_code, client_credentials, and refresh_token ..
	private Set<String> redirectUris;   // 客户端能够在基于重定向的流中使用的URIs(例如授权码授予) ...
	private Set<String> scopes;  //客户端请求的所允许的scopes ...
	private ClientSettings clientSettings;   // 客户端设置 ... (客户端的自定义设置,例如需要 PKCE https://datatracker.ietf.org/doc/html/rfc7636, 需要授权许可 authorization consent,以及其他) ..
	private TokenSettings tokenSettings; // 有关 颁发给客户端的 OAuth2 token的自定义设置, 举个例子,访问 / 访问/刷新令牌的生存时间,重用刷新token,以及其他)... 
}
```
### RegisteredClientRepository
RegisteredClientRepository 是一个中心组件(新的客户端能够注册,已有的客户端能够被查询 ...) .. 它也能够被其他的组件使用(例如当处于一个特定的协议流中,例如客户端认证,授权授予处理,token 检测,动态客户端注册以及其他) \
RegisteredClientRepository的提供实现 是一个InMemoryRegisteredClientRepository 以及 JdbcRegisteredClientRepository ... 内存型的实现将RegisteredClient存储在内存中并且仅仅在开发和测试阶段
使用,JdbcRegisteredClientRepository 是一个JDBC实现(它通过JdbcOperations 持久化 RegisteredClient 实例) .. \
RegisteredClientRepository 是一个Required 组件 ... \
以下例子展示了如何注册一个RegisteredClientRepository @Bean
```java
@Bean
public RegisteredClientRepository registeredClientRepository() {
	List<RegisteredClient> registrations = ...
	return new InMemoryRegisteredClientRepository(registrations);
}
```
除此之外,你可以配置一个RegisteredClientRepository .. 
```java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer =
		new OAuth2AuthorizationServerConfigurer<>();
	http.apply(authorizationServerConfigurer);

	authorizationServerConfigurer
		.registeredClientRepository(registeredClientRepository);

	...

	return http.build();
}
```
OAuth2AuthorizationServerConfigurer  是有用的,当你需要同时应用多个配置选项 ...

### OAuth2Authorization
OAuth2Authorization 是 OAuth2 授权的表示，在 client_credentials 授权授予类型的情况下，它保存与资源所有者或自身授予客户端的授权相关的状态。
相关的授权模型在Spring Security OAuth2客户端中的支持是 OAuth2AuthorizedClient ... \
在授权授予流成功完成之后,一个OAuth2Authorization 将被创建并关联一个OAuth2AccessToken,一个可选的OAuth2RefreshToken ... 以及一些外的特定于执行授权授予类型的状态 ...\
一个OAuth2Token 实例 关联到一个OAuth2Authorization ..,依赖于特定的授予授权类型 .. \
对于OAuth2 授权码授予, OAuth2Authorization 会关联 OAuth2RefreshToken ,OAuth2AuthorizationCode,OAuth2AccessToken(可选) .. \
对于OpenID Connect 1.0 授权码授予,一个 OAuth2AuthorizationCode, an OidcIdToken, an OAuth2AccessToken, and an (optional) OAuth2RefreshToken are associated. \
一个 OAuth2Authorization 以及它的属性定义如下:
```java
public class OAuth2Authorization implements Serializable {
	private String id;  
	private String registeredClientId;  
	private String principalName;   // 资源拥有者(或者客户端)的主要身份
	private AuthorizationGrantType authorizationGrantType;  
	private Map<Class<? extends OAuth2Token>, Token<?>> tokens; 
	private Map<String, Object> attributes; // 特定于执行授权授予类型的额外属性,认证的身份,OAuth2AuthorizationRequest,授权的scope 以及其他 ...

	...

}
```
OAuth2Authorization 以及它关联的 OAuth2Token  有一个固定的寿命, 一个新颁发的OAuth2Token 是激活的并且在它过期或者无效(撤销)之后会失活, 一个OAuth2Authorization 隐式的无效(当所有关联的
OAuth2Token 实例都无效时) .. 每一个OAuth2Token 都存放在OAuth2Authorization.Token中,这个实例提供了访问器(isExpired / isInvalidated() / isActive ...) ... \
OAuth2Authorization.Token 也提供了getClaims(),它会返回和关联的 OAuth2Token的claims ..(如果有) ..

### OAuth2AuthorizationService
它是一个中心组件, 新的授权将被存储,存在的授权将被查询, 它被其他组件使用(当位于特定的协议流中),举个例子,客户端认证,授权码授予处理,token 检测,token 撤销, 动态客户端注册以及其他 .. \
它提供了主要实现 InMemoryOAuth2AuthorizationService  和 JdbcOAuth2AuthorizationService ...
Jdbc...将 OAuth2Authorization  持久化(通过 JdbcOperations) ... \
注意: OAuth2AuthorizationService  是一个可选的组件,默认是 InMemoryOAuth2AuthorizationService ... \
如何注册一个 OAuth2AuthorizationService ...
```java
@Bean
public OAuth2AuthorizationService authorizationService() {
	return new InMemoryOAuth2AuthorizationService();
}
```
除此之外,你可以配置一个OAuth2AuthorizationService(通过  OAuth2AuthorizationServerConfigurer)
```java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer =
		new OAuth2AuthorizationServerConfigurer<>();
	http.apply(authorizationServerConfigurer);

	authorizationServerConfigurer
		.authorizationService(authorizationService);

	...

	return http.build();
}
```

### OAuth2AuthorizationConsent
它标识一个授权协商(许可)的决策(来自OAuth2 授权请求流),例如,authorization_code 授予流,它持有资源拥有者授予给客户端的权限 ... \
当授权访问一个客户端的时候,资源拥有者也许仅仅授予客户端请求的一部分权限 ... 这通常发生在 authorization_code  授予流中,通常
资源拥有者决定是否授予访问请求的scope的权限 . \
在OAuth2 授权请求流的完成之后,一个 OAuth2AuthorizationConsent 将会被创建(或者被更新) 并且关联授于的权限到客户端和资源拥有者 ... \
OAuth2AuthorizationConsent 以及它的属性定义如下 ...
```java
public final class OAuth2AuthorizationConsent implements Serializable {
	private final String registeredClientId;    
	private final String principalName; 
	private final Set<GrantedAuthority> authorities;   // 由 资源拥有者授予客户端的权限(一个权限能够标识一个scope,一个声明  / 条款,一个权限,一个角色,或者其他) ...
}
```
### OAuth2AuthorizationConsentService
OAuth2AuthorizationConsentService 是一个核心组件, 当新的授权赞许被存储 以及存在的授权赞许被查询时使用 ... \
它主要被实现 OAuth2 授权请求流的组件 使用(例如 authorization_code  授予) \
OAuth2AuthorizationConsentService 提供了两种实现,InMemoryOAuth2AuthorizationConsentService  / JdbcOAuth2AuthorizationConsentService,
InMemoryOAuth2AuthorizationConsentService  实现应该仅仅使用在内存中, JdbcOAuth2AuthorizationConsentService  是一个 JDBC 实现(它持久化了 OAuth2AuthorizationConsent 实例,它通过
JdbcOperations持久化 ...) \
它是一个可选组件,默认就是基于内存型的 .. InMemoryOAuth2AuthorizationConsentService
```java
@Bean
public OAuth2AuthorizationConsentService authorizationConsentService() {
	return new InMemoryOAuth2AuthorizationConsentService();
}
```
除此之外,你可以通过 OAuth2AuthorizationServerConfigurer 配置 OAuth2AuthorizationConsentService ...
```java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer =
		new OAuth2AuthorizationServerConfigurer<>();
	http.apply(authorizationServerConfigurer);

	authorizationServerConfigurer
		.authorizationConsentService(authorizationConsentService);

	...

	return http.build();
}
```
### OAuth2TokenContext
它是一个上下文对象持有OAuth2Token的信息并且能够被OAuth2TokenGenerator 以及 OAuth2TokenCustomizer 使用 ... \
OAuth2TokenContext 拥有以下的访问器:
```java
public interface OAuth2TokenContext extends Context {

	default RegisteredClient getRegisteredClient() ...  

	default <T extends Authentication> T getPrincipal() ...  // 资源拥有者(或者客户端)的认证实例 ..

	default ProviderContext getProviderContext() ...     // 有关Provider的上下文对象 ...

	@Nullable
	default OAuth2Authorization getAuthorization() ...   // 授权授予关联的 OAuth2Authorization ..

	default Set<String> getAuthorizedScopes() ...    // 授予给客户端的scopes ..

	default OAuth2TokenType getTokenType() ...   // Oauth2TokenType 标识生成Token的类型,例如 code,access_token,refresh_token, id_token ...

	default AuthorizationGrantType getAuthorizationGrantType() ...   和授权授予流关联的授权授予类型 ...

	default <T extends Authentication> T getAuthorizationGrant() ...     // 被AuthenticationProvider 用来处理授权授予过程的认证实例 ...

	...

}
```

### OAuth2TokenGenerator
一个OAuth2TokenGenerator 负责生成一个OAuth2Token(从提供的OAuth2TokenContext中获取包含的信息 ..) \
OAuth2Token 生成主要依赖于在OAuth2TokenContext中关联的OAuth2TokenType .. \
举个例子,OAuth2TokenType 的值是:
- code, 然后生成 OAuth2AuthorizationCode
- access_token 然后生成 OAuth2AccessToken 
- refresh_token 然后生成 OAuth2RefreshToken 
- id_token 然后生成 OidcIdToken 

因此,生成的OAuth2AccessToken格式的变换,依赖于为RegisteredClient配置的 TokenSettings.getAccessTokenFormat() ..
如果格式是OAuth2TokenFormat.SELF_CONTAINED(the default),那么生成JWT，如果格式为 OAuth2TokenFormat.REFERENCE,那么"opaque"token将会生成 ... \
最终,如果生成的OAuth2Token 有一组claims 并且实现了ClaimAccessor,那么claim能够通过OAuth2Authorization.Token.getClaims()进行访问 ... \
OAuth2TokenGenerator 主要被实现了授权授予处理的组件使用(例如 authorization_code, client_credentials, and refresh_token) .. \
提供的实现有 OAuth2AccessTokenGenerator,OAuth2RefreshTokenGenerator,JwtGenerator ... \
OAuth2AccessTokenGenerator 生成一个"opaque"(OAuth2TokenFormat.REFERENCE)访问
Token,然后JwtGenerator 生成JWT(格式为 OAuth2TokenFormat.SELF_CONTAINED) ... \
注意OAuth2TokenGenerator 是一个可选的组件并且默认是DelegatingOAuth2TokenGenerator (它组合了 OAuth2AccessTokenGenerator  以及 OAuth2RefreshTokenGenerator)\
注意,一个JwtEncoder @Bean 或者 JWKSource<SecurityContext> @Bean注册,那么JwtGenerator 将被额外的注册到 DelegatingOAuth2TokenGenerator中 ... \
OAuth2TokenGenerator 提供了巨大的灵活性,因为它能够支持token格式(不仅仅是 access_token,refresh_token) ... \
以下的例子展示了如何注册一个OAuth2TokenGenerator @Bean
```java
@Bean
public OAuth2TokenGenerator<?> tokenGenerator() {
	JwtEncoder jwtEncoder = ...
	JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
	OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
	OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
	return new DelegatingOAuth2TokenGenerator(
			jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
}
```
除此之外,你还可以配置一个 通过 OAuth2AuthorizationServerConfigurer 配置OAuth2TokenGenerator
```java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer =
		new OAuth2AuthorizationServerConfigurer<>();
	http.apply(authorizationServerConfigurer);

	authorizationServerConfigurer
		.tokenGenerator(tokenGenerator);

	...

	return http.build();
}
```

### OAuth2TokenCustomizer
它提供了一种能力定制化 OAuth2Token的属性,它能够从提供的OAuth2TokenContext中访问,它由OAuth2TokenGenerator 使用并让它定制OAuth2Token的属性(在token生成之前) ... \
一个Oauth2TokenCustomizer<Oauth2TokenClaimsContext>声明了一个泛型的OAuth2TokenClaimsContext(实现于 OAuth2TokenContext) 提供了一种能力去定制 "opaque" OAuth2AccessToken的
条款/约定(claims) ...,OAuth2TokenClaimsContext.getClaims() 提供了访问OAuth2TokenClaimsSet.Builder的能力,允许增加/ 替换/移除claims ... \
下面的例子展示了如何实现 OAuth2TokenCustomizer<OAuth2TokenClaimsContext> 并使用OAuthAccessTokenGenerator 配置它 ..
```java
@Bean
public OAuth2TokenGenerator<?> tokenGenerator() {
	JwtEncoder jwtEncoder = ...
	JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
	OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
	accessTokenGenerator.setAccessTokenCustomizer(accessTokenCustomizer());
	OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
	return new DelegatingOAuth2TokenGenerator(
			jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
}

@Bean
public OAuth2TokenCustomizer<OAuth2TokenClaimsContext> accessTokenCustomizer() {
	return context -> {
		OAuth2TokenClaimsSet.Builder claims = context.getClaims();
		// Customize claims

	};
}
```
注意到如果OAuth2TokenGenerator 不没有配置为一个@Bean,或者没有通过 OAuth2AuthorizationServerConfigurer 配置,那么 OAuth2TokenCustomizer<OAuth2TokenClaimsContext> @Bean
将会自动使用Oauth2AccessTokenGenerator 配置 ... \
一个OAuth2TokenCustomizer<JwtEncodingContext> 声明了 JwtEncodingContext泛型(它实现了 OAuth2TokenContext) 提供了一种能力自定义Jwt的headers 以及 claims,JwtEncodingContext.getHeaders()
提供了一种能力访问JwsHeader.Builder,能增加 / 替换 / 删除 headers .. \
JwtEncodingContext.getClaims() 提供了一种能力访问 JwtClaimSet.Builder,允许有能力增加 / 替换 / 删除 claims ... \
以下的例子展示了如何实现一个 OAuth2TokenCustomizer<JwtEncodingContext> 并使用JwtGenerator 配置它 ..
```java
@Bean
public OAuth2TokenGenerator<?> tokenGenerator() {
	JwtEncoder jwtEncoder = ...
	JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
	jwtGenerator.setJwtCustomizer(jwtCustomizer());
	OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
	OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
	return new DelegatingOAuth2TokenGenerator(
			jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
}

@Bean
public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
	return context -> {
		JwsHeader.Builder headers = context.getHeaders();
		JwtClaimsSet.Builder claims = context.getClaims();
		if (context.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)) {
			// Customize headers/claims for access_token

		} else if (context.getTokenType().getValue().equals(OidcParameterNames.ID_TOKEN)) {
			// Customize headers/claims for id_token

		}
	};
}
```
同样,如果OAuth2TokenGenerator 没有提供为一个@Bean,或者它没有通过 OAuth2AuthorizationServerConfigurer配置,那么 OAuth2TokenCustomizer<JwtEncodingContext> @Bean将自动
使用JwtGenerator 配置 ... \
对于如何自定义ID Token的示例,可以查看  [How-to: Customize the OpenID Connect 1.0 UserInfo response](https://docs.spring.io/spring-authorization-server/docs/current/reference/html/guides/how-to-userinfo.html#how-to-userinfo).

## 协议端点
### OAuth2 Authorization Endpoint
OAuth2AuthorizationEndpointConfigurer  提供了一种能力定制OAuth2 授权端点, 它定义了扩展点让你能够定制预处理,主要处理,后置处理逻辑(关于OAuth2 授权请求) ... \
OAuth2AuthorizationEndpointConfigurer 提供了以下的配置选项:
```java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer =
		new OAuth2AuthorizationServerConfigurer<>();
	http.apply(authorizationServerConfigurer);

	authorizationServerConfigurer
		.authorizationEndpoint(authorizationEndpoint ->
			authorizationEndpoint
				.authorizationRequestConverter(authorizationRequestConverter)    // AuthenticationConverter  预处理(从请求中抓取 OAuth2授权请求) 转换为 OAuth2AuthorizationCodeRequestAuthenticationToken
				.authenticationProvider(authenticationProvider)  // 主要处理器 用来认证 OAuth2AuthorizationCodeRequestAuthenticationToken(可以添加一个或者多个认证提供器,替换默认的) ..
				.authorizationResponseHandler(authorizationResponseHandler)  // 后置处理器, 处理认证成功的行为 OAuth2AuthorizationCodeRequestAuthenticationToken  并返回一个OAuth2AuthorizationResponse. ..
				.errorResponseHandler(errorResponseHandler)  // 失败,后置处理 OAuth2AuthorizationCodeRequestAuthenticationException 并返回OAuth2Error 响应 ..
				.consentPage("/oauth2/v1/authorize")    // 协商页面,这个URI 标识自定义的赞同页面(重定向资源拥有者,如果协商是需要的,在授权请求流期间) ..
		);

	return http.build();
}
```
OAuth2AuthorizationEndpointConfigurer  配置 OAuth2AuthorizationEndpointFilter 并注册它到 授权服务器的 SecurityFilterChain @Bean中,OAuth2AuthorizationEndpointFilter 是一个过滤器(用来处理
OAuth2 授权请求以及协商(客户端代理呈现内容,资源拥有者或者最终用户决定是否授予权限访问)) ... \
OAuth2AuthorizationEndpointFilter 默认配置如下内容:
- AuthenticationConverter - OAuth2AuthorizationCodeRequestAuthenticationConverter
- AuthenticationManager - OAuth2AuthorizationCodeRequestAuthenticationProvider 组合而成的认证管理器 ...
- AuthenticationSuccessHandler - An internal implementation that handles an “authenticated” OAuth2AuthorizationCodeRequestAuthenticationToken and returns the OAuth2AuthorizationResponse
- AuthenticationFailureHandler - 内部实现(将 OAuth2AuthorizationCodeRequestAuthenticationException  处理为 OAuth2Error)并返回OAuth2Error 响应 ...

### OAuth2 Token 端点
OAuth2TokenEndpointConfigurer  提供了能力配置OAuth2 Token 端点,它定义了扩展点用来自定义预处理 / 主要处理 / 后置处理(对于OAuth2 访问 token 请求的逻辑处理) ... \
OAuth2TokenEndpointConfigurer  提供了以下的配置选项:
```java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer =
		new OAuth2AuthorizationServerConfigurer<>();
	http.apply(authorizationServerConfigurer);

	authorizationServerConfigurer
		.tokenEndpoint(tokenEndpoint ->
			tokenEndpoint
				.accessTokenRequestConverter(accessTokenRequestConverter)    // 类似于 oauth2 authorization_code ,, 预处理器 ... 转换为 OAuth2AuthorizationGrantAuthenticationToken
				.authenticationProvider(authenticationProvider)  // 类似, 认证 OAuth2AuthorizationGrantAuthenticationToken(可以添加一个或者多个) ..
				.accessTokenResponseHandler(accessTokenResponseHandler)  // 后置处理器,处理 OAuth2AccessTokenAuthenticationToken  返回  OAuth2AccessTokenResponse ...
				.errorResponseHandler(errorResponseHandler)  // 类似,处理OAuth2AuthenticationException  返回 OAuth2Error 响应 ..
		);

	return http.build();
}
```
OAuth2TokenEndpointConfigurer 配置一个 OAuth2TokenEndpointFilter 并通过OAuth2 授权服务器的 SecurityFilterChain @Bean 注入 ... 这个过滤器处理 OAuth2 access token 请求 ... \
支持的授权授予类型有 authorization_code, refresh_token, and client_credentials ... \
OAuth2TokenEndpointFilter 包含以下内容:
- AuthenticationConverter DelegatingAuthenticationConverter 代理 OAuth2AuthorizationCodeAuthenticationConverter, OAuth2RefreshTokenAuthenticationConverter, and OAuth2ClientCredentialsAuthenticationConverter
- AuthenticationManager  AuthenticationManager  代理 OAuth2AuthorizationCodeAuthenticationProvider,OAuth2RefreshTokenAuthenticationProvider, and OAuth2ClientCredentialsAuthenticationProvider ..
- AuthenticationSuccessHandler 内部实现它将处理 OAuth2AccessTokenAuthenticationToken  并返回一个 OAuth2AccessTokenResponse ..
- AuthenticationFailureHandler 内部实现将处理 OAuth2AuthenticationException 并返回OAuth2Error 响应 ..

### OAuth2 Token Introspection Endpoint 
OAuth2TokenIntrospectionEndpointConfigurer  提供了定制这个端点的能力,它定义了一些扩展点让你能够自定义预处理 / 主要处理/ 后置处理 Oauth2 introspection 请求 ..
OAuth2TokenIntrospectionEndpointConfigurer 配置如下:
```java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer =
		new OAuth2AuthorizationServerConfigurer<>();
	http.apply(authorizationServerConfigurer);

	authorizationServerConfigurer
		.tokenIntrospectionEndpoint(tokenIntrospectionEndpoint ->
			tokenIntrospectionEndpoint
				.introspectionRequestConverter(introspectionRequestConverter)   // 请求转换器,转换为 OAuth2TokenIntrospectionAuthenticationToken ..
				.authenticationProvider(authenticationProvider) // 认证 OAuth2TokenIntrospectionAuthenticationToken,可以提供一个或者多个替换默认实现 ..
				.introspectionResponseHandler(introspectionResponseHandler)  // 成功处理器 ..返回  OAuth2TokenIntrospection response ..
				.errorResponseHandler(errorResponseHandler)  // 错误处理器,处理 OAuth2AuthenticationException 并返回 OAuth2Error 响应 ..
		);

	return http.build();
}
```
OAuth2TokenIntrospectionEndpointConfigurer  同样会配置 OAuth2TokenIntrospectionEndpointFilter  将它注册到授权服务器的 SecurityFilterChain @Bean ...
它主要用来处理OAuth2 introspection requests .. \
OAuth2TokenIntrospectionEndpointFilter 配置默认如下：
- AuthenticationConverter 默认内部实现返回 OAuth2TokenIntrospectionAuthenticationToken.
- AuthenticationManager,默认组合 OAuth2TokenIntrospectionAuthenticationProvider 的认证管理器 ..
- AuthenticationSuccessHandler 处理一个认证的 OAuth2TokenIntrospectionAuthenticationToken ,并返回 OAuth2TokenIntrospection 响应 ..
- AuthenticationFailureHandler 处理一个 OAuth2AuthenticationException  错误并返回 OAuth2Error 响应 ...

### OAuth2 Token Revocation Endpoint
OAuth2TokenRevocationEndpointConfigurer ...类似 ... 用来配置处理 OAuth2 撤销请求的逻辑 .. \
OAuth2TokenRevocationEndpointConfigurer 配置如下:
```java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer =
		new OAuth2AuthorizationServerConfigurer<>();
	http.apply(authorizationServerConfigurer);

	authorizationServerConfigurer
		.tokenRevocationEndpoint(tokenRevocationEndpoint ->
			tokenRevocationEndpoint
				.revocationRequestConverter(revocationRequestConverter)    // 将 请求转换为 OAuth2TokenRevocationAuthenticationToken
				.authenticationProvider(authenticationProvider)  // 认证 OAuth2TokenRevocationAuthenticationToken
				.revocationResponseHandler(revocationResponseHandler)  // 成功处理器  并返回  OAuth2 revocation response
				.errorResponseHandler(errorResponseHandler)  // 处理 OAuth2AuthenticationException  并返回 OAuth2Error 响应 ..
		);

	return http.build();
```
OAuth2TokenRevocationEndpointFilter 默认配置如下:
- AuthenticationConverter - An internal implementation that returns the OAuth2TokenRevocationAuthenticationToken.
- AuthenticationManager - OAuth2TokenRevocationAuthenticationProvider 组合而成的认证管理器
- AuthenticationSuccessHandler - 内部实现,处理认证的 OAuth2TokenRevocationAuthenticationToken 并返回 OAuth2 revocation 响应 ..
- AuthenticationFailureHandler - OAuth2AuthenticationException 处理并返回 OAuth2Error 响应 ..

### OAuth2 Authorization Server Metadata Endpoint
OAuth2AuthorizationServerConfigurer 提供了对OAuth2 授权服务器元数据端点的支持 .. \
OAuth2AuthorizationServerConfigurer 配置一个 OAuth2AuthorizationServerMetadataEndpointFilter 并将它注册到 OAuth2授权服务器 SecurityFilterChain @Bean中,
它处理  OAuth2 authorization server metadata requests 并返回 OAuth2AuthorizationServerMetadata 响应 ...

### JWK Set 端点
OAuth2AuthorizationServerConfigurer  提供对 JWK Set endpoint的配置支持 ..
OAuth2AuthorizationServerConfigurer 会配置一个NimbusJwkSetEndpointFilter 并注册到授权服务器中的SecurityFilterChain中, 它负责返回 [JWK Set](https://datatracker.ietf.org/doc/html/rfc7517#section-5). \
如果JWKSource<SecurityContext>配置才会配置JWKSet 端点 ...

### OpenID Connect 1.0 Provider 配置端点
OidcConfigurer 提供了配置 [OpenID Connect 1.0 Provider Configuration endpoint](https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfig)的配置支持 \
OidcConfigurer 将配置一个 OidcProviderConfigurationEndpointFilter 并注册它到 OAuth2授权服务器的 SecurityFilterChain中,OidcProviderConfigurationEndpointFilter 用来处理
OidcProviderConfiguration response. 
### OpenID Connect 1.0 UserInfo Endpoint
OidcUserInfoEndpointConfigurer  提供了一种能力去定制 OpenID Connect 1.0 用户信息端点,它定义了一个扩展点,能够自定义 UserInfo 响应 .. \
OidcUserInfoEndpointConfigurer 配置如下属性:
```java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer =
		new OAuth2AuthorizationServerConfigurer<>();
	http.apply(authorizationServerConfigurer);

	authorizationServerConfigurer
		.oidc(oidc ->
			oidc
				.userInfoEndpoint(userInfoEndpoint ->
					userInfoEndpoint.userInfoMapper(userInfoMapper)    // 从 OidcUserInfoAuthenticationContext  抓取 claims到 OidcUserInfo ...
				)
		);

	return http.build();
}
```
OidcUserInfoEndpointConfigurer 配置了一个 OidcUserInfoEndpointFilter 并注册它到授权服务器的 SecurityFilterChain @Bean,OidcUserInfoEndpointFilter 用来处理 [ UserInfo requests](https://openid.net/specs/openid-connect-core-1_0.html#UserInfoRequest) 并返回
[OidcUserInfo response](https://openid.net/specs/openid-connect-core-1_0.html#UserInfoResponse) ... \
OidcUserInfoEndpointFilter 配置默认如下:
- AuthenticationManager - 包含了 OidcUserInfoAuthenticationProvider 它关联了一个内部的 userInfoMapper 用来从标准的ID Token中基于当前授权的请求的scope 抓取标准的 claims ... 

你能够定制ID Token 通过提供  OAuth2TokenCustomizer<JwtEncodingContext> @Bean ... \
OpenID Connect 1.0 Userinfo 端点是一个Oauth2 保护资源,这需要访问token(作为 userinfo 请求的 bearer token),以下示例展示了如何启用OAuth2 资源服务器配置:
```java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer =
		new OAuth2AuthorizationServerConfigurer<>();
	http.apply(authorizationServerConfigurer);

	...

	http.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);

	return http.build();
}

@Bean
public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
	return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
}
```
对于OpenID Connect 1.0 UserInfo 端点,JwtDecoder @Bean 是必须的 .. \
如何定制UserInfo 端点响应的示例查看 [ How-to: Customize the OpenID Connect 1.0 UserInfo response ](https://docs.spring.io/spring-authorization-server/docs/current/reference/html/guides/how-to-userinfo.html#how-to-userinfo)

### OpenID Connect 1.0 客户端 Registration Endpoint
OidcClientRegistrationEndpointConfigurer 能够配置 [OpenID Connect 1.0 Client Registration endpoint](https://openid.net/specs/openid-connect-registration-1_0.html#ClientRegistration) ..
以下示例展示了如何启用(默认是禁用的) OpenID Connect 1.0 Client Registration endpoint:
```java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer =
		new OAuth2AuthorizationServerConfigurer<>();
	http.apply(authorizationServerConfigurer);

	authorizationServerConfigurer
		.oidc(oidc ->
			oidc
				.clientRegistrationEndpoint(Customizer.withDefaults())
		);

	return http.build();
}
```
OpenID Connect 1.0 客户端注册端点默认禁用(因为大多数部署不需要动态客户端注册) ... \
OidcClientRegistrationEndpointConfigurer 配置了 OidcClientRegistrationEndpointFilter 并注册到授权服务器的SecurityFilterChain @Bean中 .. \
它主要用来处理 [Client Registration requests](https://openid.net/specs/openid-connect-registration-1_0.html#RegistrationRequest) 并返回 [OidcClientRegistration response](https://openid.net/specs/openid-connect-registration-1_0.html#RegistrationResponse) ..\
OidcClientRegistrationEndpointFilter 同样处理 [Client Read Requests](https://openid.net/specs/openid-connect-registration-1_0.html#ReadRequest) 并返回 [OidcClientRegistration response](https://openid.net/specs/openid-connect-registration-1_0.html#ReadResponse) ... \
OidcClientRegistrationEndpointFilter 默认配置如下:
- AuthenticationManager - OidcClientRegistrationAuthenticationProvider组合而成的 认证管理器 ...

OpenID Connect 1.0 Client Registration endpoint 是一个OAuth2 保护资源,它需要在客户端注册或者(client read)请求中 设置bearer token ...  \
访问 token在client registration request中需要 OAuth2 scope: client.create; \
访问token 在client Read 请求中需要 OAuth2 scope: client.read; \
以下例子展示了如何启用OAuth2 资源服务器配置:
```java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer =
		new OAuth2AuthorizationServerConfigurer<>();
	http.apply(authorizationServerConfigurer);

	...

	http.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);

	return http.build();
}

@Bean
public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
	return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
}
```
对于OpenID Connect 1.0 客户端注册端点来说,JwtDecoder 是必须的 ..


## How-to: Customize the OpenID Connect 1.0 UserInfo response
这个指南叫我们如何自定义Spring 授权服务器的UserInfo 端点, 这个指南的主要目的是说明如何启用端点并使用可用的定制选择去生产自定义的响应 ...
- 启用 User Info 端点
- 自定义User Info 响应 ..

### 启用端点
这是一个OAuth2 保护资源, 需要访问token 设置到 userinfo 请求上 (通过 bearer token的形式) .. \
从OpenID Connect 认证请求中获取的访问Token必须设置为bearer Token ...per Section 2 of OAuth 2.0 Bearer Token Usage [RFC6750]. .. \
在定制之前,你需要启用UserInfo 端点,这是一件很简单的事情:
```java
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ClientSettings;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
public class EnableUserInfoSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .exceptionHandling((exceptions) -> exceptions
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails userDetails = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(userDetails);
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("messaging-client")
                .clientSecret("{noop}secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUri("http://127.0.0.1:8080/login/oauth2/code/messaging-client-oidc")
                .redirectUri("http://127.0.0.1:8080/authorized")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.ADDRESS)
                .scope(OidcScopes.EMAIL)
                .scope(OidcScopes.PHONE)
                .scope(OidcScopes.PROFILE)
                .scope("message.read")
                .scope("message.write")
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .build();

        return new InMemoryRegisteredClientRepository(registeredClient);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        }
        catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    @Bean
    public ProviderSettings providerSettings() {
        return ProviderSettings.builder().build();
    }

}
```
其中的UserService用来允许用户认证去获取访问 token ...  \
OAuth2ResourceServerConfigurer::jwt 是为了userInfo 请求能够使用accessToken 进行认证 .. \
JwtDecoder 用来验证访问 token  ...
### 定制user info 响应
- 定制 id Token
- 定制user Info Mapper

#### 定制 ID token
默认情况userinfo 响应将会生成一个 id_token放置在token响应中,它使用claims进行生成 , 使用默认策略,标准的claims 仅仅基于授权期间请求的scope 返回到user info 响应中 ... \
首选自定义user info的响应是通过增加标准的 claims到 id_token中,以下展示了如何增加claims到 id_token中:
```java
@Configuration
public class IdTokenCustomizerConfig {

	@Bean 
	public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer(
			OidcUserInfoService userInfoService) {
		return (context) -> {
			if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
				OidcUserInfo userInfo = userInfoService.loadUser( 
						context.getPrincipal().getName());
				context.getClaims().claims(claims ->
						claims.putAll(userInfo.getClaims()));
			}
		};
	}

}
```
通过 OAuth2TokenCustomizer定制器定制 id_token .. \
一个自定义的获取用户信息的服务被使用 .. \
以下展示了自定义用户服务:
```java
/**
 * Example service to perform lookup of user info for customizing an {@code id_token}.
 */
@Service
public class OidcUserInfoService {

	private final UserInfoRepository userInfoRepository = new UserInfoRepository();

	public OidcUserInfo loadUser(String username) {
		return new OidcUserInfo(this.userInfoRepository.findByUsername(username));
	}

	static class UserInfoRepository {

		private final Map<String, Map<String, Object>> userInfo = new HashMap<>();

		public UserInfoRepository() {
			this.userInfo.put("user1", createUser("user1"));
			this.userInfo.put("user2", createUser("user2"));
		}

		public Map<String, Object> findByUsername(String username) {
			return this.userInfo.get(username);
		}

		private static Map<String, Object> createUser(String username) {
			return OidcUserInfo.builder()
					.subject(username)
					.name("First Last")
					.givenName("First")
					.familyName("Last")
					.middleName("Middle")
					.nickname("User")
					.preferredUsername(username)
					.profile("https://example.com/" + username)
					.picture("https://example.com/" + username + ".jpg")
					.website("https://example.com")
					.email(username + "@example.com")
					.emailVerified(true)
					.gender("female")
					.birthdate("1970-01-01")
					.zoneinfo("Europe/Paris")
					.locale("en-US")
					.phoneNumber("+1 (604) 555-1234;ext=5678")
					.phoneNumberVerified("false")
					.claim("address", Collections.singletonMap("formatted", "Champ de Mars\n5 Av. Anatole France\n75007 Paris\nFrance"))
					.updatedAt("1970-01-01T00:00:00Z")
					.build()
					.getClaims();
		}
	}

}
```

#### 定制 UserInfo Mapper
为了完全自定义用户info 响应,需要提供自定义的 user info mapper 它能够生成对象用来渲染 响应,这是一个OidcUserInfo实例(它来自于Spring Security), 这个mapper 实现接收一个
OidcUserInfoAuthenticationContext 的实例(它包含当前请求的信息),包括OAuth2Authorization ... \
以下的例子展示了如何使用自定义选项(可用的) 直接在 OAuth2AuthorizationServerConfigurer 进行配置:
```java
@Configuration
public class JwtUserInfoMapperSecurityConfig {

	@Bean 
	@Order(1)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
		OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer =
				new OAuth2AuthorizationServerConfigurer<>();
		RequestMatcher endpointsMatcher = authorizationServerConfigurer
				.getEndpointsMatcher();

		Function<OidcUserInfoAuthenticationContext, OidcUserInfo> userInfoMapper = (context) -> { 
			OidcUserInfoAuthenticationToken authentication = context.getAuthentication();
			JwtAuthenticationToken principal = (JwtAuthenticationToken) authentication.getPrincipal();

			return new OidcUserInfo(principal.getToken().getClaims());
		};

		authorizationServerConfigurer
			.oidc((oidc) -> oidc
				.userInfoEndpoint((userInfo) -> userInfo
					.userInfoMapper(userInfoMapper) 
				)
			);
		http
			.requestMatcher(endpointsMatcher)
			.authorizeRequests((authorize) -> authorize
				.anyRequest().authenticated()
			)
			.csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
			.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt) 
			.exceptionHandling((exceptions) -> exceptions
				.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
			)
			.apply(authorizationServerConfigurer); 

		return http.build();
	}

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails userDetails = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(userDetails);
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("messaging-client")
                .clientSecret("{noop}secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUri("http://127.0.0.1:8080/login/oauth2/code/messaging-client-oidc")
                .redirectUri("http://127.0.0.1:8080/authorized")
                .scope(OidcScopes.OPENID)
                .scope("message.read")
                .scope("message.write")
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .build();

        return new InMemoryRegisteredClientRepository(registeredClient);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        }
        catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    @Bean
    public ProviderSettings providerSettings() {
        return ProviderSettings.builder().build();
    }

}
```
这个配置从access token中映射 claims()(它是一个JWT(在指南的开始示例中)) 它填充用户信息响应并提供以下内容:
- 协议端点的Spring security 过滤链
- 映射claims的 user info mapper 
- 自定义user info mapper的配置选项
- 资源服务器 支持 允许 User info 请求能够使用access token 认证 ..
- 如何应用 OAuth2AuthorizationServerConfigurer  到 Spring Security 配置上 ..

这个userInfo mapper 不限制于 映射来自JWT的claims,这仅仅是一个示例说明如何定制,类似于定制ID token的claims,你能够定制访问 token的 claims(提前),例如:
```java
@Configuration
public class JwtTokenCustomizerConfig {

	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
		return (context) -> {
			if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
				context.getClaims().claims((claims) -> {
					claims.put("claim-1", "value-1");
					claims.put("claim-2", "value-2");
				});
			}
		};
	}

}
```
无论你是直接定制 user info 响应 或者 使用这个示例并自定义访问token,你能够在数据库中查询信息,执行一个 LDAP 查询,发送一个请求到其他服务,或者使用任何你想放置在 user info 响应中的其他含义的信息 ...

### How-To: Implementation core services with JPA
这个指南告诉你如何实现Spring 授权服务器的核心服务(通过JPA), 这个指南的主要目的是提供一个实现这些服务的起点,你可以跟随你的想法定制它们 ..
- 定义数据模型
- 创建JPA 实体
- 创建Spring Data 仓库
- 实现核心服务

### 定义数据模型
除了token,state,metadata,settings,claims values,我们使用JPA column 默认 255长度(为所有的columns), 事实上这个长度你可以随意定制 ..
- 客户端schema
- 授权 schema
- 授权 协商 schema
### 客户端schema
RegisteredClient 它包含了少量的多值 字段以及某些settings 字段(需要存储任意 key/value 数据),以下是一个示例
```sql
CREATE TABLE client (
    id varchar(255) NOT NULL,
    clientId varchar(255) NOT NULL,
    clientIdIssuedAt timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    clientSecret varchar(255) DEFAULT NULL,
    clientSecretExpiresAt timestamp DEFAULT NULL,
    clientName varchar(255) NOT NULL,
    clientAuthenticationMethods varchar(1000) NOT NULL,
    authorizationGrantTypes varchar(1000) NOT NULL,
    redirectUris varchar(1000) DEFAULT NULL,
    scopes varchar(1000) NOT NULL,
    clientSettings varchar(2000) NOT NULL,
    tokenSettings varchar(2000) NOT NULL,
    PRIMARY KEY (id)
);
```
### 授权 schema
OAuth2Authorization 领域对象更加复杂,它包含了各种多值字段同样各种任意长度的 token 值,metadata,settings以及 claims 值, 内置的 JDBC实现利用了 扁平化的结构(它更喜欢性能而不是标准化),同样可以适配 .. \
它很难发现一个扁平化的数据库schema(在所有的情况以及在所有的数据库厂商中工作的非常好),你也许需要格式化或者 极大的修改你的schema(来得到更好的使用) ...

```sql
CREATE TABLE authorization (
    id varchar(255) NOT NULL,
    registeredClientId varchar(255) NOT NULL,
    principalName varchar(255) NOT NULL,
    authorizationGrantType varchar(255) NOT NULL,
    attributes varchar(4000) DEFAULT NULL,
    state varchar(500) DEFAULT NULL,
    authorizationCodeValue varchar(4000) DEFAULT NULL,
    authorizationCodeIssuedAt timestamp DEFAULT NULL,
    authorizationCodeExpiresAt timestamp DEFAULT NULL,
    authorizationCodeMetadata varchar(2000) DEFAULT NULL,
    accessTokenValue varchar(4000) DEFAULT NULL,
    accessTokenIssuedAt timestamp DEFAULT NULL,
    accessTokenExpiresAt timestamp DEFAULT NULL,
    accessTokenMetadata varchar(2000) DEFAULT NULL,
    accessTokenType varchar(255) DEFAULT NULL,
    accessTokenScopes varchar(1000) DEFAULT NULL,
    refreshTokenValue varchar(4000) DEFAULT NULL,
    refreshTokenIssuedAt timestamp DEFAULT NULL,
    refreshTokenExpiresAt timestamp DEFAULT NULL,
    refreshTokenMetadata varchar(2000) DEFAULT NULL,
    oidcIdTokenValue varchar(4000) DEFAULT NULL,
    oidcIdTokenIssuedAt timestamp DEFAULT NULL,
    oidcIdTokenExpiresAt timestamp DEFAULT NULL,
    oidcIdTokenMetadata varchar(2000) DEFAULT NULL,
    oidcIdTokenClaims varchar(2000) DEFAULT NULL,
    PRIMARY KEY (id)
);
```
### 授权 consent schema
OAuth2AuthorizationConsent 是一个简单的模型,它仅仅包含单个多值字段(除了组合key之外),以下展示了 authorizationConsent  schema ..
```sql
CREATE TABLE authorizationConsent (
    registeredClientId varchar(255) NOT NULL,
    principalName varchar(255) NOT NULL,
    authorities varchar(1000) NOT NULL,
    PRIMARY KEY (registeredClientId, principalName)
);
```
## 创建 JPA entities
通过 JPA 自动生成 schema ...
- client Entity
- Authorization Entity
- Authorization Consent Entity

### Client Entity
以下展示了 Client entity, 它从  RegisteredClient 持久化 ..

```java
@Entity
@Table(name = "`client`")
public class Client {
	@Id
	private String id;
	private String clientId;
	private Instant clientIdIssuedAt;
	private String clientSecret;
	private Instant clientSecretExpiresAt;
	private String clientName;
	@Column(length = 1000)
	private String clientAuthenticationMethods;
	@Column(length = 1000)
	private String authorizationGrantTypes;
	@Column(length = 1000)
	private String redirectUris;
	@Column(length = 1000)
	private String scopes;
	@Column(length = 2000)
	private String clientSettings;
	@Column(length = 2000)
	private String tokenSettings;

}
```

### 授权实体
它从OAuth2Authorization 映射
```java
@Entity
@Table(name = "`authorization`")
public class Authorization {
	@Id
	@Column
	private String id;
	private String registeredClientId;
	private String principalName;
	private String authorizationGrantType;
	@Column(length = 4000)
	private String attributes;
	@Column(length = 500)
	private String state;

	@Column(length = 4000)
	private String authorizationCodeValue;
	private Instant authorizationCodeIssuedAt;
	private Instant authorizationCodeExpiresAt;
	private String authorizationCodeMetadata;

	@Column(length = 4000)
	private String accessTokenValue;
	private Instant accessTokenIssuedAt;
	private Instant accessTokenExpiresAt;
	@Column(length = 2000)
	private String accessTokenMetadata;
	private String accessTokenType;
	@Column(length = 1000)
	private String accessTokenScopes;

	@Column(length = 4000)
	private String refreshTokenValue;
	private Instant refreshTokenIssuedAt;
	private Instant refreshTokenExpiresAt;
	@Column(length = 2000)
	private String refreshTokenMetadata;

	@Column(length = 4000)
	private String oidcIdTokenValue;
	private Instant oidcIdTokenIssuedAt;
	private Instant oidcIdTokenExpiresAt;
	@Column(length = 2000)
	private String oidcIdTokenMetadata;
	@Column(length = 2000)
	private String oidcIdTokenClaims;

}
```

### Authorization Consent Entity
OAuth2AuthorizationConsent 映射持久化数据信息
```java
@Entity
@Table(name = "`authorizationConsent`")
@IdClass(AuthorizationConsent.AuthorizationConsentId.class)
public class AuthorizationConsent {
	@Id
	private String registeredClientId;
	@Id
	private String principalName;
	@Column(length = 1000)
	private String authorities;

	public static class AuthorizationConsentId implements Serializable {
		private String registeredClientId;
		private String principalName;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			AuthorizationConsentId that = (AuthorizationConsentId) o;
			return registeredClientId.equals(that.registeredClientId) && principalName.equals(that.principalName);
		}

		@Override
		public int hashCode() {
			return Objects.hash(registeredClientId, principalName);
		}
	}
}
```

## 创建Spring Data 仓库
### Client Repository
```java
@Repository
public interface ClientRepository extends JpaRepository<Client, String> {
	Optional<Client> findByClientId(String clientId);
}
```
## Authorization Repository
通过id 查询, 以及额外的各种token 字段查询 Authorization ..
```java
@Repository
public interface AuthorizationRepository extends JpaRepository<Authorization, String> {
	Optional<Authorization> findByState(String state);
	Optional<Authorization> findByAuthorizationCodeValue(String authorizationCode);
	Optional<Authorization> findByAccessTokenValue(String accessToken);
	Optional<Authorization> findByRefreshTokenValue(String refreshToken);
	@Query("select a from Authorization a where a.state = :token" +
			" or a.authorizationCodeValue = :token" +
			" or a.accessTokenValue = :token" +
			" or a.refreshTokenValue = :token"
	)
	Optional<Authorization> findByStateOrAuthorizationCodeValueOrAccessTokenValueOrRefreshTokenValue(@Param("token") String token);
}
```
### Authorization Consent Repository
```java
@Repository
public interface AuthorizationConsentRepository extends JpaRepository<AuthorizationConsent, AuthorizationConsent.AuthorizationConsentId> {
	Optional<AuthorizationConsent> findByRegisteredClientIdAndPrincipalName(String registeredClientId, String principalName);
	void deleteByRegisteredClientIdAndPrincipalName(String registeredClientId, String principalName);
}
```
## 实现核心的服务
现在能够实现核心服务,通过 审查 Jdbc实现,我们能够编写一些工具类用来转换枚举值以及读取或者写入JSON 数据(为属性,settings / metadata /claims 字段) ... \
但是,请记住，将 JSON 数据写入具有固定长度的文本列已证明 Jdbc 实现存在问题,这个示例仍然这样做,你也许需要分离这些字段到单独的表或者(支持任意长度数据值的)数据存储中 ...
### Registered Client Repository
它持久化一个Client 并从 Client 映射 RegisteredClient 领域对象数据 ...(它内部使用 ClientRepository)
```java
@Component
public class JpaRegisteredClientRepository implements RegisteredClientRepository {
	private final ClientRepository clientRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public JpaRegisteredClientRepository(ClientRepository clientRepository) {
		Assert.notNull(clientRepository, "clientRepository cannot be null");
		this.clientRepository = clientRepository;

		ClassLoader classLoader = JpaRegisteredClientRepository.class.getClassLoader();
		List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
		this.objectMapper.registerModules(securityModules);
		this.objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
	}

	@Override
	public void save(RegisteredClient registeredClient) {
		Assert.notNull(registeredClient, "registeredClient cannot be null");
		this.clientRepository.save(toEntity(registeredClient));
	}

	@Override
	public RegisteredClient findById(String id) {
		Assert.hasText(id, "id cannot be empty");
		return this.clientRepository.findById(id).map(this::toObject).orElse(null);
	}

	@Override
	public RegisteredClient findByClientId(String clientId) {
		Assert.hasText(clientId, "clientId cannot be empty");
		return this.clientRepository.findByClientId(clientId).map(this::toObject).orElse(null);
	}

	private RegisteredClient toObject(Client client) {
		Set<String> clientAuthenticationMethods = StringUtils.commaDelimitedListToSet(
				client.getClientAuthenticationMethods());
		Set<String> authorizationGrantTypes = StringUtils.commaDelimitedListToSet(
				client.getAuthorizationGrantTypes());
		Set<String> redirectUris = StringUtils.commaDelimitedListToSet(
				client.getRedirectUris());
		Set<String> clientScopes = StringUtils.commaDelimitedListToSet(
				client.getScopes());

		RegisteredClient.Builder builder = RegisteredClient.withId(client.getId())
				.clientId(client.getClientId())
				.clientIdIssuedAt(client.getClientIdIssuedAt())
				.clientSecret(client.getClientSecret())
				.clientSecretExpiresAt(client.getClientSecretExpiresAt())
				.clientName(client.getClientName())
				.clientAuthenticationMethods(authenticationMethods ->
						clientAuthenticationMethods.forEach(authenticationMethod ->
								authenticationMethods.add(resolveClientAuthenticationMethod(authenticationMethod))))
				.authorizationGrantTypes((grantTypes) ->
						authorizationGrantTypes.forEach(grantType ->
								grantTypes.add(resolveAuthorizationGrantType(grantType))))
				.redirectUris((uris) -> uris.addAll(redirectUris))
				.scopes((scopes) -> scopes.addAll(clientScopes));

		Map<String, Object> clientSettingsMap = parseMap(client.getClientSettings());
		builder.clientSettings(ClientSettings.withSettings(clientSettingsMap).build());

		Map<String, Object> tokenSettingsMap = parseMap(client.getTokenSettings());
		builder.tokenSettings(TokenSettings.withSettings(tokenSettingsMap).build());

		return builder.build();
	}

	private Client toEntity(RegisteredClient registeredClient) {
		List<String> clientAuthenticationMethods = new ArrayList<>(registeredClient.getClientAuthenticationMethods().size());
		registeredClient.getClientAuthenticationMethods().forEach(clientAuthenticationMethod ->
				clientAuthenticationMethods.add(clientAuthenticationMethod.getValue()));

		List<String> authorizationGrantTypes = new ArrayList<>(registeredClient.getAuthorizationGrantTypes().size());
		registeredClient.getAuthorizationGrantTypes().forEach(authorizationGrantType ->
				authorizationGrantTypes.add(authorizationGrantType.getValue()));

		Client entity = new Client();
		entity.setId(registeredClient.getId());
		entity.setClientId(registeredClient.getClientId());
		entity.setClientIdIssuedAt(registeredClient.getClientIdIssuedAt());
		entity.setClientSecret(registeredClient.getClientSecret());
		entity.setClientSecretExpiresAt(registeredClient.getClientSecretExpiresAt());
		entity.setClientName(registeredClient.getClientName());
		entity.setClientAuthenticationMethods(StringUtils.collectionToCommaDelimitedString(clientAuthenticationMethods));
		entity.setAuthorizationGrantTypes(StringUtils.collectionToCommaDelimitedString(authorizationGrantTypes));
		entity.setRedirectUris(StringUtils.collectionToCommaDelimitedString(registeredClient.getRedirectUris()));
		entity.setScopes(StringUtils.collectionToCommaDelimitedString(registeredClient.getScopes()));
		entity.setClientSettings(writeMap(registeredClient.getClientSettings().getSettings()));
		entity.setTokenSettings(writeMap(registeredClient.getTokenSettings().getSettings()));

		return entity;
	}

	private Map<String, Object> parseMap(String data) {
		try {
			return this.objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {
			});
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	private String writeMap(Map<String, Object> data) {
		try {
			return this.objectMapper.writeValueAsString(data);
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	private static AuthorizationGrantType resolveAuthorizationGrantType(String authorizationGrantType) {
		if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(authorizationGrantType)) {
			return AuthorizationGrantType.AUTHORIZATION_CODE;
		} else if (AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equals(authorizationGrantType)) {
			return AuthorizationGrantType.CLIENT_CREDENTIALS;
		} else if (AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(authorizationGrantType)) {
			return AuthorizationGrantType.REFRESH_TOKEN;
		}
		return new AuthorizationGrantType(authorizationGrantType);              // Custom authorization grant type
	}

	private static ClientAuthenticationMethod resolveClientAuthenticationMethod(String clientAuthenticationMethod) {
		if (ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue().equals(clientAuthenticationMethod)) {
			return ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
		} else if (ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue().equals(clientAuthenticationMethod)) {
			return ClientAuthenticationMethod.CLIENT_SECRET_POST;
		} else if (ClientAuthenticationMethod.NONE.getValue().equals(clientAuthenticationMethod)) {
			return ClientAuthenticationMethod.NONE;
		}
		return new ClientAuthenticationMethod(clientAuthenticationMethod);      // Custom client authentication method
	}
}
```

### Authorization Service
JpaOAuth2AuthorizationService 使用  AuthorizationRepository 持久化Authorization 或者获取Authorization ...
```java
@Component
public class JpaOAuth2AuthorizationService implements OAuth2AuthorizationService {
	private final AuthorizationRepository authorizationRepository;
	private final RegisteredClientRepository registeredClientRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public JpaOAuth2AuthorizationService(AuthorizationRepository authorizationRepository, RegisteredClientRepository registeredClientRepository) {
		Assert.notNull(authorizationRepository, "authorizationRepository cannot be null");
		Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null");
		this.authorizationRepository = authorizationRepository;
		this.registeredClientRepository = registeredClientRepository;

		ClassLoader classLoader = JpaOAuth2AuthorizationService.class.getClassLoader();
		List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
		this.objectMapper.registerModules(securityModules);
		this.objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
	}

	@Override
	public void save(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");
		this.authorizationRepository.save(toEntity(authorization));
	}

	@Override
	public void remove(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");
		this.authorizationRepository.deleteById(authorization.getId());
	}

	@Override
	public OAuth2Authorization findById(String id) {
		Assert.hasText(id, "id cannot be empty");
		return this.authorizationRepository.findById(id).map(this::toObject).orElse(null);
	}

	@Override
	public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
		Assert.hasText(token, "token cannot be empty");

		Optional<Authorization> result;
		if (tokenType == null) {
			result = this.authorizationRepository.findByStateOrAuthorizationCodeValueOrAccessTokenValueOrRefreshTokenValue(token);
		} else if (OAuth2ParameterNames.STATE.equals(tokenType.getValue())) {
			result = this.authorizationRepository.findByState(token);
		} else if (OAuth2ParameterNames.CODE.equals(tokenType.getValue())) {
			result = this.authorizationRepository.findByAuthorizationCodeValue(token);
		} else if (OAuth2ParameterNames.ACCESS_TOKEN.equals(tokenType.getValue())) {
			result = this.authorizationRepository.findByAccessTokenValue(token);
		} else if (OAuth2ParameterNames.REFRESH_TOKEN.equals(tokenType.getValue())) {
			result = this.authorizationRepository.findByRefreshTokenValue(token);
		} else {
			result = Optional.empty();
		}

		return result.map(this::toObject).orElse(null);
	}

	private OAuth2Authorization toObject(Authorization entity) {
		RegisteredClient registeredClient = this.registeredClientRepository.findById(entity.getRegisteredClientId());
		if (registeredClient == null) {
			throw new DataRetrievalFailureException(
					"The RegisteredClient with id '" + entity.getRegisteredClientId() + "' was not found in the RegisteredClientRepository.");
		}

		OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient)
				.id(entity.getId())
				.principalName(entity.getPrincipalName())
				.authorizationGrantType(resolveAuthorizationGrantType(entity.getAuthorizationGrantType()))
				.attributes(attributes -> attributes.putAll(parseMap(entity.getAttributes())));
		if (entity.getState() != null) {
			builder.attribute(OAuth2ParameterNames.STATE, entity.getState());
		}

		if (entity.getAuthorizationCodeValue() != null) {
			OAuth2AuthorizationCode authorizationCode = new OAuth2AuthorizationCode(
					entity.getAuthorizationCodeValue(),
					entity.getAuthorizationCodeIssuedAt(),
					entity.getAuthorizationCodeExpiresAt());
			builder.token(authorizationCode, metadata -> metadata.putAll(parseMap(entity.getAuthorizationCodeMetadata())));
		}

		if (entity.getAccessTokenValue() != null) {
			OAuth2AccessToken accessToken = new OAuth2AccessToken(
					OAuth2AccessToken.TokenType.BEARER,
					entity.getAccessTokenValue(),
					entity.getAccessTokenIssuedAt(),
					entity.getAccessTokenExpiresAt(),
					StringUtils.commaDelimitedListToSet(entity.getAccessTokenScopes()));
			builder.token(accessToken, metadata -> metadata.putAll(parseMap(entity.getAccessTokenMetadata())));
		}

		if (entity.getRefreshTokenValue() != null) {
			OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(
					entity.getRefreshTokenValue(),
					entity.getRefreshTokenIssuedAt(),
					entity.getRefreshTokenExpiresAt());
			builder.token(refreshToken, metadata -> metadata.putAll(parseMap(entity.getRefreshTokenMetadata())));
		}

		if (entity.getOidcIdTokenValue() != null) {
			OidcIdToken idToken = new OidcIdToken(
					entity.getOidcIdTokenValue(),
					entity.getOidcIdTokenIssuedAt(),
					entity.getOidcIdTokenExpiresAt(),
					parseMap(entity.getOidcIdTokenClaims()));
			builder.token(idToken, metadata -> metadata.putAll(parseMap(entity.getOidcIdTokenMetadata())));
		}

		return builder.build();
	}

	private Authorization toEntity(OAuth2Authorization authorization) {
		Authorization entity = new Authorization();
		entity.setId(authorization.getId());
		entity.setRegisteredClientId(authorization.getRegisteredClientId());
		entity.setPrincipalName(authorization.getPrincipalName());
		entity.setAuthorizationGrantType(authorization.getAuthorizationGrantType().getValue());
		entity.setAttributes(writeMap(authorization.getAttributes()));
		entity.setState(authorization.getAttribute(OAuth2ParameterNames.STATE));

		OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode =
				authorization.getToken(OAuth2AuthorizationCode.class);
		setTokenValues(
				authorizationCode,
				entity::setAuthorizationCodeValue,
				entity::setAuthorizationCodeIssuedAt,
				entity::setAuthorizationCodeExpiresAt,
				entity::setAuthorizationCodeMetadata
		);

		OAuth2Authorization.Token<OAuth2AccessToken> accessToken =
				authorization.getToken(OAuth2AccessToken.class);
		setTokenValues(
				accessToken,
				entity::setAccessTokenValue,
				entity::setAccessTokenIssuedAt,
				entity::setAccessTokenExpiresAt,
				entity::setAccessTokenMetadata
		);
		if (accessToken != null && accessToken.getToken().getScopes() != null) {
			entity.setAccessTokenScopes(StringUtils.collectionToDelimitedString(accessToken.getToken().getScopes(), ","));
		}

		OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken =
				authorization.getToken(OAuth2RefreshToken.class);
		setTokenValues(
				refreshToken,
				entity::setRefreshTokenValue,
				entity::setRefreshTokenIssuedAt,
				entity::setRefreshTokenExpiresAt,
				entity::setRefreshTokenMetadata
		);

		OAuth2Authorization.Token<OidcIdToken> oidcIdToken =
				authorization.getToken(OidcIdToken.class);
		setTokenValues(
				oidcIdToken,
				entity::setOidcIdTokenValue,
				entity::setOidcIdTokenIssuedAt,
				entity::setOidcIdTokenExpiresAt,
				entity::setOidcIdTokenMetadata
		);
		if (oidcIdToken != null) {
			entity.setOidcIdTokenClaims(writeMap(oidcIdToken.getClaims()));
		}

		return entity;
	}

	private void setTokenValues(
			OAuth2Authorization.Token<?> token,
			Consumer<String> tokenValueConsumer,
			Consumer<Instant> issuedAtConsumer,
			Consumer<Instant> expiresAtConsumer,
			Consumer<String> metadataConsumer) {
		if (token != null) {
			OAuth2Token oAuth2Token = token.getToken();
			tokenValueConsumer.accept(oAuth2Token.getTokenValue());
			issuedAtConsumer.accept(oAuth2Token.getIssuedAt());
			expiresAtConsumer.accept(oAuth2Token.getExpiresAt());
			metadataConsumer.accept(writeMap(token.getMetadata()));
		}
	}

	private Map<String, Object> parseMap(String data) {
		try {
			return this.objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {
			});
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	private String writeMap(Map<String, Object> metadata) {
		try {
			return this.objectMapper.writeValueAsString(metadata);
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	private static AuthorizationGrantType resolveAuthorizationGrantType(String authorizationGrantType) {
		if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(authorizationGrantType)) {
			return AuthorizationGrantType.AUTHORIZATION_CODE;
		} else if (AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equals(authorizationGrantType)) {
			return AuthorizationGrantType.CLIENT_CREDENTIALS;
		} else if (AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(authorizationGrantType)) {
			return AuthorizationGrantType.REFRESH_TOKEN;
		}
		return new AuthorizationGrantType(authorizationGrantType);              // Custom authorization grant type
	}
}
```

### Authorization Consent Service
以下使用了 JpaOAuth2AuthorizationConsentService 使用 AuthorizationConsentRepository 持久化  AuthorizationConsent 并映射 或者 反序列化获取 ..
```java
@Component
public class JpaOAuth2AuthorizationConsentService implements OAuth2AuthorizationConsentService {
	private final AuthorizationConsentRepository authorizationConsentRepository;
	private final RegisteredClientRepository registeredClientRepository;

	public JpaOAuth2AuthorizationConsentService(AuthorizationConsentRepository authorizationConsentRepository, RegisteredClientRepository registeredClientRepository) {
		Assert.notNull(authorizationConsentRepository, "authorizationConsentRepository cannot be null");
		Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null");
		this.authorizationConsentRepository = authorizationConsentRepository;
		this.registeredClientRepository = registeredClientRepository;
	}

	@Override
	public void save(OAuth2AuthorizationConsent authorizationConsent) {
		Assert.notNull(authorizationConsent, "authorizationConsent cannot be null");
		this.authorizationConsentRepository.save(toEntity(authorizationConsent));
	}

	@Override
	public void remove(OAuth2AuthorizationConsent authorizationConsent) {
		Assert.notNull(authorizationConsent, "authorizationConsent cannot be null");
		this.authorizationConsentRepository.deleteByRegisteredClientIdAndPrincipalName(
				authorizationConsent.getRegisteredClientId(), authorizationConsent.getPrincipalName());
	}

	@Override
	public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
		Assert.hasText(registeredClientId, "registeredClientId cannot be empty");
		Assert.hasText(principalName, "principalName cannot be empty");
		return this.authorizationConsentRepository.findByRegisteredClientIdAndPrincipalName(
				registeredClientId, principalName).map(this::toObject).orElse(null);
	}

	private OAuth2AuthorizationConsent toObject(AuthorizationConsent authorizationConsent) {
		String registeredClientId = authorizationConsent.getRegisteredClientId();
		RegisteredClient registeredClient = this.registeredClientRepository.findById(registeredClientId);
		if (registeredClient == null) {
			throw new DataRetrievalFailureException(
					"The RegisteredClient with id '" + registeredClientId + "' was not found in the RegisteredClientRepository.");
		}

		OAuth2AuthorizationConsent.Builder builder = OAuth2AuthorizationConsent.withId(
				registeredClientId, authorizationConsent.getPrincipalName());
		if (authorizationConsent.getAuthorities() != null) {
			for (String authority : StringUtils.commaDelimitedListToSet(authorizationConsent.getAuthorities())) {
				builder.authority(new SimpleGrantedAuthority(authority));
			}
		}

		return builder.build();
	}

	private AuthorizationConsent toEntity(OAuth2AuthorizationConsent authorizationConsent) {
		AuthorizationConsent entity = new AuthorizationConsent();
		entity.setRegisteredClientId(authorizationConsent.getRegisteredClientId());
		entity.setPrincipalName(authorizationConsent.getPrincipalName());

		Set<String> authorities = new HashSet<>();
		for (GrantedAuthority authority : authorizationConsent.getAuthorities()) {
			authorities.add(authority.getAuthority());
		}
		entity.setAuthorities(StringUtils.collectionToCommaDelimitedString(authorities));

		return entity;
	}
}
```