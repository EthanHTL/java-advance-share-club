# Authorization Events
每一个被拒绝的授权,都会派发出一个 AuthorizationDeniedEvent.. 同样也可能触发授权成功的事件 ..

为了监听这些事件,你必须发布一个 AuthorizationEventPublisher ..

spring security的Spring AuthorizationEventPublisher  可能是适合的 ..

它使用spring 的ApplicationEventPublisher 发布授权事件 ..
```java
@Bean
public AuthorizationEventPublisher authorizationEventPublisher
        (ApplicationEventPublisher applicationEventPublisher) {
    return new SpringAuthorizationEventPublisher(applicationEventPublisher);
}
```
然后你可以使用spring的@EventListener 支持
```java
@Component
public class AuthenticationEvents {

    @EventListener
    public void onFailure(AuthorizationDeniedEvent failure) {
		// ...
    }
}
```
## 授权授予的事件
因为AuthorizationGrantedEvent 可能最终非常的烦躁 .. 它们默认没有发布 ..

事实上, 发布这些事件将可能需要某一些业务逻辑作为其中一部分来确保你的应用不会在嘈杂的授权事件中淹没 ..

你能够创建你自己的事件派发器 来触发这些事件 .. 例如,以下的派发器仅仅发布授权授予成功事件(仅当ROLE_ADMIN 存在)
```java
@Component
public class MyAuthorizationEventPublisher implements AuthorizationEventPublisher {
    private final ApplicationEventPublisher publisher;
    private final AuthorizationEventPublisher delegate;

    public MyAuthorizationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
        this.delegate = new SpringAuthorizationEventPublisher(publisher);
    }

    @Override
    public <T> void publishAuthorizationEvent(Supplier<Authentication> authentication,
            T object, AuthorizationDecision decision) {
        if (decision == null) {
            return;
        }
        if (!decision.isGranted()) {
            this.delegate.publishAuthorizationEvent(authentication, object, decision);
            return;
        }
        if (shouldThisEventBePublished(decision)) {
            AuthorizationGrantedEvent granted = new AuthorizationGrantedEvent(
                    authentication, object, decision);
            this.publisher.publishEvent(granted);
        }
    }

    private boolean shouldThisEventBePublished(AuthorizationDecision decision) {
        if (!(decision instanceof AuthorityAuthorizationDecision)) {
            return false;
        }
        Collection<GrantedAuthority> authorities = ((AuthorityAuthorizationDecision) decision).getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
```