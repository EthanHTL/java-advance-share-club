package club.smileboy.websocket.app.config;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.ExceptionWebSocketHandlerDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.jetty.JettyRequestUpgradeStrategy;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.web.socket.server.support.WebSocketHttpRequestHandler;

/**
 * @author jasonj
 * @date 2024/6/20
 * @time 15:22
 * @description web socket 配置器
 * <p>
 * <p>
 * WebSocketSession(底层如果是standard WebSocket session (JSR-356)) 需要同步进行消息的发送,不支持并发发送
 * <p>
 * 通过 ConcurrentWebSocketSessionDecorator. 进行包装
 * @see WebSocketHttpRequestHandler
 * @see WebSocketConfigurer
 * @see org.springframework.web.socket.WebSocketHandler
 * @see org.springframework.web.socket.handler.WebSocketHandlerDecorator
 * @see HandshakeInterceptor
 * @see ConcurrentWebSocketSessionDecorator
 * @see WebSocketSession
 * @see org.springframework.web.socket.server.HandshakeHandler
 * @see org.springframework.web.socket.server.RequestUpgradeStrategy
 *
 *          RequestUpgradeStrategy 主要是用来解决在JSR-356的运行时的情况下 如何 让WebSocket API参与到 web环境中 ..
 *          servlet api 提供了两种机制,一个是Servlet container classpath scan (a Servlet 3 feature)
 *          一个是:  Servlet container initialization（ServletContainerInitializer）. 但是他们都没有提供一种方式通过一个前端控制器来处理所有的websocket 和http请求 ..
 *
 *          于是 spring 提出了这个升级策略,能够解决上面部署形式的限制之外,还能够在新版本 As of Jakarta WebSocket 2.1,
 *          a standard request upgrade strategy is available which Spring chooses on Jakarta EE 10 based web containers such as Tomcat 10.1 and Jetty 12.
 *
 *          也就是说 jakarta websocket 2.1开始之后, servlet相关的相关规范支持标准的请求升级策略 ..
 *
 *          其次,Servlet container version with JSR-356 support 会进行类路径扫描，会导致启动速度比较慢，如果是这样的话 可以选择性的启用或关闭
 *          web 碎片(或者sci 扫描)  - 通过 <absolute-ordering /> element in web.xml的使用
 *
 *          例如选择性启用某些模块的类路径扫描,指定jar 名称即可
 *          <pre>
 *                  <web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
 * 	                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 * 	                    xsi:schemaLocation="
 * 		                https://jakarta.ee/xml/ns/jakartaee
 * 		                https://jakarta.ee/xml/ns/jakartaee/web-app_5_0.xsd"
 * 	                    version="5.0">
 *
 *              	<absolute-ordering>
 * 		                <name>spring_web</name>
 * 	                </absolute-ordering>
 *
 *                  </web-app>
 *          </pre>
 **/
@EnableWebSocket
@Configuration
public class WebSocketEndpointConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        // 处理器 除了实现本身的功能之外,还可以添加装饰器来实现额外的功能,例如心跳重连,或者其他操作
        // 默认的WebSocketHandlerDecorator 的装饰器    ExceptionWebSocketHandlerDecorator 装饰了 webSocketHandler,并且提供了
        //默认的行为,捕获从任何webSocketHandler中抛出的异常并且使用状态101关闭了webSocket会话 .. 这指示了一个服务器行为 ..
        registry.addHandler(new ExceptionWebSocketHandlerDecorator(
                        new TextWebSocketHandler() {
                            @Override
                            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                                System.out.println("接收到websocket 消息" + message.getPayload());
                                session.sendMessage(new PingMessage());
                            }
                        }
                ), "/ws")
                // 设置跨域模式
//                .setAllowedOrigins("*")
                // 增加特定的拦截器,用来拦截(preclude) 握手(将一些http session的属性添加到 websocket session中)
                .addInterceptors(new HttpSessionHandshakeInterceptor());
        // 还可以设定特定的 DefaultHandshakeHandler 扩展,来进行websocket的握手,包括验证来源(client origin),协商子协议,以及其他细节.
        // 用来配置一个特定的RequestUpgradeStrategy 例如适配一个特定的websocket server 和一个不受支持的版本.

//                .setHandshakeHandler();
    }


    // config websocket server

    // You can configure of the underlying WebSocket server such as input message buffer size, idle timeout, and more.
    // For Jakarta WebSocket servers, you can add a ServletServerContainerFactoryBean to your Java configuration. For example:
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192);
        container.setMaxBinaryMessageBufferSize(8192);

        // 会话空闲超时
//        container.setMaxSessionIdleTimeout();
        // 异步发送超时
//        container.setAsyncSendTimeout();
        return container;
    }


    // For client Jakarta WebSocket configuration, use ContainerProvider.getWebSocketContainer() in Java configuration,
    // or WebSocketContainerFactoryBean in XML.
//    @Bean
//    public ContainerProvider containerProvider() {
//        WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
//    }

    // 在学习的时候,发现jetty相关的代码 和最新版的文档对不上,也许需要切换spring 相关版本依赖来领略 新版本功能
    // 例如servlet 对标准协议的支持,所以可能会有一些对策略的配置 ..
//    JettyRequestUpgradeStrategy
//    @Bean
//    public DefaultHandshakeHandler handshakeHandler() {
//        JettyRequestUpgradeStrategy strategy = new JettyRequestUpgradeStrategy();
//        strategy.addWebSocketConfigurer(configurable -> {
//            policy.setInputBufferSize(8192);
//            policy.setIdleTimeout(600000);
//        });
//        return new DefaultHandshakeHandler(strategy);
//    }


    // 跨域问题
    //  三种行为
//        1. 默认行为 仅相同域下的请求,这种情况下 sockJS 启用,并且iframe http响应将会设置为同域,(这个iframe嵌入的页面其实是服务器提供的,提供了
    // 消息发送和消息接收的一些默认配置), 仅在同域的情况下 这种才安全,其次jsonp (本身不会检查请求的origin)本身不是很安全,同域下保证安全所以禁用它 .. 这是spring 说的 ..

    // (由于这些安全措施和技术实现，SockJS在启用同源请求限制模式后，不支持IE6和IE7。这些浏览器缺乏对现代安全机制和API的支持，使得SockJS无法在这些环境中安全、有效地工作。),所以不支持在这种环境下使用

//         2. 特定源 必须以 `http` 或者 ·`https` 开头的源   ,但是iframe在这种模式下禁用了,所以  IE6 through IE9 are not supported
//            3. 所有域 域的值为(*) 所有传输将支持 ..

}
