package club.smileboy.websocket.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.sockjs.SockJsService;

import javax.swing.*;

/**
 * @author jasonj
 * @date 2024/6/20
 * @time 18:01
 * @description sockJS  socket js 通信兜底
 *
 * 也就是说sockJS 是为了在公共网络上(在不受自己控制的情况下),对于websocket 交互失败的情况下,通过websocket 仿真(模拟)
 *
 * 注意: 在公共 Internet 上，您无法控制的限制性代理可能会阻止 WebSocket 交互，因为它们未配置为传递 Upgrade 标头，或者因为它们关闭了看似空闲的长期连接。
 *
 * 这个问题的解决方案是 WebSocket 模拟——也就是说，首先尝试使用 WebSocket，然后再使用基于 HTTP 的技术来模拟 WebSocket 交互并公开相同的应用程序级 API。
 *
 *
 * 为了实现这个目的, spring的servlet stack 系列下, 在服务端和客户端都提供了SockJS protocol的支持 ..
 *
 *
 * SockJS 的目标是让应用程序使用 WebSocket API，但在运行时必要时可以回退到非 WebSocket 替代方案，而无需更改应用程序代码。
 *
 * SockJS 协议以可执行叙述测试的形式定义。
 * 并且有支持这个协议的js库 ..
 *
 * spring-websocket 提供了对sockJS 服务器的实现
 *
 * 从spring-websocket 4.1 开始,存在 socketJs 的java客户端的实现
 *
 * 技术分为: WebSocket, HTTP Streaming, and HTTP Long Polling.
 *
 *
 * 机制: 1. 首先通过一个端点 GET /info 获取基本信息,然后决定 采用那种传输方式
 * If possible, WebSocket is used. If not, in most browsers, there is at least one HTTP streaming option. If not, then HTTP (long) polling is used.
 *
 * 所有传输请求格式为： https://host:port/myApp/myEndpoint/{server-id}/{session-id}/{transport}
 *
 *
 * SockJS 添加了最少的消息帧。例如，服务器最初发送字母 o（“打开”帧），消息以 [“message1”，“message2”]（JSON 编码数组）发送，
 * 如果没有消息，则发送字母 h（“心跳”帧）流 25 秒（默认情况下），字母 c（“关闭”帧）用于关闭会话。
 *
 *
 * 这个配置和 WebsocketEndpointConfig 任意开一个即可(但是我们下面使用了其他路径,都可以开)
 *
 *
 * ie8-ie9(如果服务器不需要cookie,The SockJS client supports Ajax/XHR streaming in IE 8 and 9 by using Microsoft’s XDomainRequest), 但是如果需要cookie
 * (跨域工作是不支持发送cookie的）, Spring’s SockJS support (sessionCookieNeeded 属性如果不需要cookie 可以关闭) ..
 *
 * 否则sockJs 可能只能选择 iframe-based transport(但是这个在浏览器中有限制,为了阻止点击劫持)
 *
 * 如果您的应用程序添加了 X-Frame-Options 响应标头（它应该如此！）并依赖于基于 iframe 的传输，
 * 则需要将标头值设置为 SAMEORIGIN 或 ALLOW-FROM <origin>。 Spring SockJS 支持还需要知道 SockJS 客户端的位置，因为它是从 iframe 加载的。
 * 默认情况下，iframe 设置为从 CDN 位置下载 SockJS 客户端。最好将此选项配置为使用与应用程序同源的 URL。
 *
 * @see org.springframework.web.socket.sockjs.support.SockJsHttpRequestHandler 能够帮助我们在任何支持http的服务器中集成sockJs server支持.
 **/
@Configuration
@EnableWebSocket
public class SockJSFallbackConfig implements WebSocketConfigurer {


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myHandler(), "/sockjs-ws").withSockJS();
    }

    @Bean
    public WebSocketHandler myHandler() {
        return new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
               session.sendMessage(new PingMessage());
            }
        };
    }



    // 如果为了支持 sockJS stack,并且为了在浏览器中使用,并且如果选择iframe-transport 那么 默认情况下sockjs-client 是从cdn下载的,
    // 我们可以设置为给定位置的sockjs-client 加载下载效率

    // 在初始开发过程中，请启用 SockJS 客户端开发模式，以防止浏览器缓存原本会被缓存的 SockJS 请求（如 iframe）。
    // 有关如何启用它的详细信息，请参阅 SockJS 客户端页面。
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/portfolio").withSockJS()
//                .setClientLibraryUrl("http://localhost:8080/myapp/js/sockjs-client.js");
//    }

    // 当通过 WebSocket 和 SockJS 使用 STOMP 时，如果 STOMP 客户端和服务器协商要交换的心跳，则 SockJS 心跳将被禁用。
    // 心跳,默认通过TaskScheduler 调度

    // 客户端取消连接 怎么检测:

    // Client Disconnects
    // 在 Servlet 容器中，这是通过 Servlet 3 异步支持完成的，该支持允许退出 Servlet 容器线程、处理请求并继续写入另一个线程的响应。
    // 一个具体问题是 Servlet API 不会为已离开的客户端提供通知。请参阅 eclipse-ee4j/servlet-api#44。但是，Servlet 容器在后续尝试写入响应时会引发异常。
    // 由于 Spring 的 SockJS 服务支持服务器发送的心跳（默认情况下每 25 秒一次），这意味着通常会在该时间段内（或更早，如果消息发送更频繁的话）检测到客户端断开连接。


    // 跨域支持 SockJS and CORS
    //如果您允许跨源请求（请参阅允许的源），SockJS 协议将使用 CORS 在 XHR 流和轮询传输中提供跨域支持。
    //因此，除非检测到响应中存在 CORS 标头，否则会自动添加 CORS 标头。因此，如果应用程序已经配置为提供 CORS 支持（例如，通过 Servlet Filter），
    //Spring 的 SockJsService 会跳过这一部分。 (SockJsService 它来帮我们自动添加跨域响应头)
}
