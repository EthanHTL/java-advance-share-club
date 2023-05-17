# saml 2.0 metadata
spring security 解析断言方的元数据去产生一个AssertingPartyDetails  实例 - 同样也会根据RelyingPartyRegistration 实例发送依赖方元数据 ..

## 解析 <saml2:IDPSSODescriptor> 元数据
你能够解析一个断言方的元数据(通过使用RelyingPartyRegistrations) ..

当使用OpenSAML 厂商支持时, 导致AssertingPartyDetails 将会是OpenSamlAssertingPartyDetails ..
这意味着你能够获得底层的 OpenSAML XMLObject:
```java
OpenSamlAssertingPartyDetails details = (OpenSamlAssertingPartyDetails)
        registration.getAssertingPartyDetails();
EntityDescriptor openSamlEntityDescriptor = details.getEntityDescriptor();
```
## 产生<saml2:SPSSSODescriptor> 元数据

你能够发送一个元数据(使用saml2Metadata DSL方法),如下所示:
```java
http
    // ...
    .saml2Login(withDefaults())
    .saml2Metadata(withDefaults());
```
你能够使用元数据端点你去注册你的依赖方以及断言方, 这能够经常很容易发现正确的表单字段(根据提供的元数据端点) ..

默认情况,元数据端点是`/saml2/metadata` ,虽然它也对应 /saml2/metadata/{registrationId} 以及  /saml2/service-provider-metadata/{registrationId} ..
前面说过,依赖方和服务提供者是同一种意思 ..

我们可以在dsl中改变 metadataUrl
```java
.saml2Metadata((saml2) -> saml2.metadataUrl("/saml/metadata"))
```

## 改变查询 RelyingPartyRegistration 的方式

如果对于识别被使用的RelyingPartyRegistration的不同策略,你能够配置自定义的 Saml2MetadataResponseResolver ..
```java
@Bean
Saml2MetadataResponseResolver metadataResponseResolver(RelyingPartyRegistrationRepository registrations) {
	RequestMatcherMetadataResponseResolver metadata = new RequestMatcherMetadataResponseResolver(
			(id) -> registrations.findByRegistrationId("relying-party"));
	metadata.setMetadataFilename("metadata.xml");
	return metadata;
}
```
因为依赖方存在元数据端点提供依赖方的元数据 ..




