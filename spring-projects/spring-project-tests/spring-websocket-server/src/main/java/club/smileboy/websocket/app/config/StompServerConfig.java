package club.smileboy.websocket.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompDecoder;
import org.springframework.messaging.simp.stomp.StompReactorNettyCodec;
import org.springframework.messaging.simp.stomp.StompTcpMessageCodec;
import org.springframework.messaging.tcp.reactor.ReactorNetty2TcpClient;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.jetty.JettyRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 * @author jasonj
 * @date 2024/6/21
 * @time 09:55
 * @description stomp server
 *
 *  WebSocket 协议定义了两种类型的消息（文本和二进制），但它们的内容未定义。该协议定义了一种机制，让客户端和服务器协商一个子协议（即更高层的消息协议），
 *  用于在 WebSocket 之上定义各自可以发送什么类型的消息、每条消息的格式是什么、消息内容是什么等等。
 *  子协议的使用是可选的，但无论哪种方式，客户端和服务器都需要就定义消息内容的某种协议达成一致。
 *
 *
 *  STOMP（面向简单文本的消息传递协议）最初是为脚本语言（例如 Ruby、Python 和 Perl）创建的，用于连接到企业消息代理。它旨在解决常用消息传递模式的最小子集。
 *  STOMP 可用于任何可靠的双向流网络协议，例如 TCP 和 WebSocket。尽管 STOMP 是面向文本的协议，但消息有效负载可以是文本或二进制。
 *
 *  STOMP 帧:
 *  <pre>
 *      COMMAND
 *      header1:value1
 *      header2:value2
 *
 *      Body^@
 *  </pre>
 *
 *  可以包含 destination 请求头(标识消息是什么并且 应该被谁消息)
 *
 *  当您使用 Spring 的 STOMP 支持时，Spring WebSocket 应用程序充当客户端的 STOMP 代理。
 *  消息被路由到 @Controller 消息处理方法或一个简单的内存代理，该代理跟踪订阅并向订阅用户广播消息。
 *
 *  当然我们可以配置Spring和一个专门的代理进行工作 .STOMP broker: (RabbitMQ, ActiveMQ, and others)
 *
 *  在broker的情况下: Spring maintains TCP connections to the broker,把获得的消息传输给broker(中继),并且将来自broker的消息往下传递给客户端
 *
 *  这样做的目的是: Spring Web 应用程序可以依赖基于 HTTP 的统一安全性、通用验证和熟悉的消息处理编程模型。
 *
 *
 * STOMP 规范中故意不透明目的地的含义。它可以是任何字符串，并且完全由 STOMP 服务器来定义它们支持的目标的语义和语法。
 * 然而，目的地通常是类似路径的字符串，其中 /topic/.. 表示发布-订阅（一对多），而 /queue/ 表示点对点（一对一）消息交流。
 *
 * STOMP 服务器可以使用 MESSAGE 命令向所有订阅者广播消息。以下示例显示服务器向订阅的客户端发送股票报价：
 * <pre>
 *     MESSAGE
 *      message-id:nxahklf6-1
 *      subscription:sub-1
 *      destination:/topic/price.stock.MMM
 *
 *      {"ticker":"MMM","price":129.45}^@
 * </pre>
 *
 * 服务器不能发送未经请求的消息。来自服务器的所有消息都必须响应特定的客户端订阅，并且服务器消息的订阅标头必须与客户端订阅的 id 标头匹配。
 * 使用stomp的好处:
 * 无需发明自定义消息传递协议和消息格式。
 * STOMP 客户端（包括 Spring 框架中的 Java 客户端）可用。
 * 您可以（可选）使用消息代理（例如 RabbitMQ、ActiveMQ 等）来管理订阅和广播消息。
 * 应用程序逻辑可以组织在任意数量的 @Controller 实例中，并且可以根据 STOMP 目标标头将消息路由到它们，而不是使用给定连接的单个 WebSocketHandler 处理原始 WebSocket 消息。
 * 您可以使用 Spring Security 根据 STOMP 目标和消息类型来保护消息。
 **/

@Configuration
@EnableWebSocketMessageBroker
public class StompServerConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // websocket endpoint
        registry.addEndpoint("/ws-stomp");
    }
    // 也就是说 stomp 本身需要broker 来进行消息路由

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 将以/topic 和 /queue 开头的主题的消息 通过给定broker 进行广播和路由(以及客户端的订阅)
        // 对于内置简单代理， /topic 和 /queue 前缀没有任何特殊含义。它们只是区分发布-订阅与点对点消息传递（即多个订阅者与一个消费者）的约定。
        // 当您使用外部代理时，请检查代理的 STOMP 页面以了解其支持哪种 STOMP 目标和前缀。
        registry.enableSimpleBroker("/topic","/queue");
        // 可以将app开头的消息路由到 @Controller中的@MessageMapping方法中
        registry.setApplicationDestinationPrefixes("/app");

        // 您还可以使用 virtualHost 属性配置 STOMP 代理中继。该属性的值设置为每个 CONNECT 帧的主机标头，
        // 并且很有用（例如，在云环境中，建立 TCP 连接的实际主机与提供基于云的 STOMP 服务的主机不同） ）。
//        registry.enableStompBrokerRelay()
//                .setVirtualHost()
//                .setRelayHost()
//                .setTcpClient(new ReactorNetty2TcpClient<>(() -> {},new StompReactorNettyCodec()));
    }
    // 指定消息处理器
    @Bean
    public DefaultHandshakeHandler handshakeHandler() {
        JettyRequestUpgradeStrategy strategy = new JettyRequestUpgradeStrategy();
        strategy.addWebSocketConfigurer(configurable -> {
//            policy.setInputBufferSize(4 * 8192);
//            policy.setIdleTimeout(600000);
        });
        return new DefaultHandshakeHandler(strategy);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setMessageSizeLimit(4 * 8192);
        // 设置发送第一个消息的时间
        registry.setTimeToFirstMessage(30000);
    }

    
    // Flow of Messages
    // 说明了 几大组件
    // 消息 /  消息管道 / 消息处理器  / 具有消息处理器订阅者的消息管道
    // Message: Simple representation for a message, including headers and payload.
    //
    //MessageHandler: Contract for handling a message.
    //
    //MessageChannel: Contract for sending a message that enables loose coupling between producers and consumers.
    //
    //SubscribableChannel: MessageChannel with MessageHandler subscribers.
    //
    //ExecutorSubscribableChannel: SubscribableChannel that uses an Executor for delivering messages.


    // 接受客户端消息的管道叫做  clientInboundChannel
    // 发送给客户端消息的管道叫做  clientOutboundChannel
    // 服务端发送消息给消息给消息代理的管道叫做 brokerChannel

    // 然后服务端 可以将消息先发送给@MessageMapping 进行消息处理 然后发送给brokerChannel -> broker
    // 也可以直接发送给 - broker 由broker 广播或者发送消息给订阅者

    // 其次由于用的是相同的controller, 也可以响应http 请求并发送消息给broker去广播消息或者发送消息给客户端
    // 借助SimpMessagingTemplate;( 或者限定名称 brokerMessagingTemplate - 例如指定了其他合适的Template 对象）



    // broker 的作用
    //内置的简单消息代理处理来自客户端的订阅请求，将它们存储在内存中，并将消息广播到具有匹配目的地的已连接客户端。
    // 代理支持类似路径的目的地，包括订阅 Ant 风格的目的地模式.

    // 如果配置了任务调度程序，简单代理支持 STOMP 心跳。要配置调度程序，您可以声明自己的 TaskScheduler bean 并通过 MessageBrokerRegistry 设置它。或者，您可以使用内置 WebSocket 配置中自动声明的配置，但是，
    // 您需要 @Lazy 来避免内置 WebSocket 配置和 WebSocketMessageBrokerConfigurer 之间的循环。例如：

    // 本质上来说 stomp 和websocket的本质区别就是 broker 就是 messageHandler(它细化了通过broker 来进行高级协议的处理等等细节)
    //STOMP 代理中继是一个 Spring MessageHandler，它通过将消息转发到外部消息代理来处理消息。为此，它与代理建立 TCP 连接，将所有消息转发给它，然后通过 WebSocket 会话将从代理接收到的所有消息转发给客户端。本质上，它充当双向转发消息的“中继”。

    // Add io.projectreactor.netty:reactor-netty and io.netty:netty-all dependencies to your project for TCP connection management.

    // 此外，应用程序组件（例如 HTTP 请求处理方法、业务服务等）还可以将消息发送到代理中继（如发送消息中所述），以将消息广播到订阅的 WebSocket 客户端。
    //实际上，代理中继可以实现稳健且可扩展的消息广播。


    // Connecting to a Broker
    // A STOMP broker relay maintains a single “system” TCP connection to the broker. This connection is used for messages originating from the server-side application only, not for receiving messages. You can configure the STOMP credentials (that is, the STOMP frame login and passcode headers) for this connection. This is exposed in both the XML namespace and Java configuration as the systemLogin and systemPasscode properties with default values of guest and guest.
    //
    //The STOMP broker relay also creates a separate TCP connection for every connected WebSocket client. You can configure the STOMP credentials that are used for all TCP connections created on behalf of clients. This is exposed in both the XML namespace and Java configuration as the clientLogin and clientPasscode properties with default values of guest and guest.
    //
    //The STOMP broker relay always sets the login and passcode headers on every CONNECT frame that it forwards to the broker on behalf of clients. Therefore, WebSocket clients need not set those headers. They are ignored. As the Authentication section explains, WebSocket clients should instead rely on HTTP authentication to protect the WebSocket endpoint and establish the client identity.
    //The STOMP broker relay also sends and receives heartbeats to and from the message broker over the “system” TCP connection. You can configure the intervals for sending and receiving heartbeats (10 seconds each by default). If connectivity to the broker is lost, the broker relay continues to try to reconnect, every 5 seconds, until it succeeds.
    //
    //Any Spring bean can implement ApplicationListener<BrokerAvailabilityEvent> to receive notifications when the “system” connection to the broker is lost and re-established. For example, a Stock Quote service that broadcasts stock quotes can stop trying to send messages when there is no active “system” connection.
    //
    //By default, the STOMP broker relay always connects, and reconnects as needed if connectivity is lost, to the same host and port. If you wish to supply multiple addresses, on each attempt to connect, you can configure a supplier of addresses, instead of a fixed host and port. The following example shows how to do that:
    //
    //@Configuration
    //@EnableWebSocketMessageBroker
    //public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {
    //
    //	// ...
    //
    //	@Override
    //	public void configureMessageBroker(MessageBrokerRegistry registry) {
    //		registry.enableStompBrokerRelay("/queue/", "/topic/").setTcpClient(createTcpClient());
    //		registry.setApplicationDestinationPrefixes("/app");
    //	}
    //
    //	private ReactorNettyTcpClient<byte[]> createTcpClient() {
    //		return new ReactorNettyTcpClient<>(
    //				client -> client.addressSupplier(() -> ... ),
    //				new StompReactorNettyCodec());
    //	}
    //}
    //You can also configure the STOMP broker relay with a virtualHost property. The value of this property is set as the host header of every CONNECT frame and can be useful (for example, in a cloud environment where the actual host to which the TCP connection is established differs from the host that provides the cloud-based STOMP service).


    // 匹配路径表达式风格
    // 可以是 '/' 也可以 '.'
    // 另一方面，"简单代理 "确实依赖于已配置的 PathMatcher，因此，如果切换分隔符，这一变化也适用于代理以及代理将消息中的目的地与订阅中的模式相匹配的方式。


    // 认证这一方面的事情  要么用普通的认证方式,或者  token 认证
    // 由于websocket 并没有规定握手的时候如何认证,包括 web客户端仅能发送请求头 以及请求参数(并且有时不支持自定义请求头发送)
    // 但是sockJs 客户端不支持请求头发送(但是java stomp 客户端支持发送http 请求头),那么可能通过请求参数进行token 传递,那么这个token可能会被后台无意识得记录 ,这可能是个缺点 ..

    // 详情查看 https://docs.spring.io/spring-framework/reference/web/websocket/stomp/authentication-token-based.html

    // 其次是通过stomp header 进行认证,那么仅有connect 帧发送的时候我们可以进行消息拦截并进行认证 ..
    // 1. 需要stomp 客户端在连接时进行 认证请求头发送
    // 2. ChannelInterceptor. 进行拦截处理 ..

    // 例如
    // @Configuration
    //@EnableWebSocketMessageBroker
    //public class MyConfig implements WebSocketMessageBrokerConfigurer {
    //
    //	@Override
    //	public void configureClientInboundChannel(ChannelRegistration registration) {
    //		registration.interceptors(new ChannelInterceptor() {
    //			@Override
    //			public Message<?> preSend(Message<?> message, MessageChannel channel) {
    //				StompHeaderAccessor accessor =
    //						MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    //				if (StompCommand.CONNECT.equals(accessor.getCommand())) {
    //					Authentication user = ... ; // access authentication header(s)
    //					accessor.setUser(user);
    //				}
    //				return message;
    //			}
    //		});
    //	}
    //}

    // 另请注意，当您使用 Spring Security 的消息授权时(也就是集成了spring security 进行其他授权时)，目前您需要确保身份验证 ChannelInterceptor 配置的顺序排在 Spring Security 之前
    // 。最好通过在 WebSocketMessageBrokerConfigurer 自己的实现中声明自定义拦截器来完成此操作，并用 @Order(Ordered.HIGHEST_PRECEDENCE + 99) 进行标记。

     // 认证/ 授权应该都发生在连接过程中 ..
    // Spring Security provides WebSocket sub-protocol authorization that uses a ChannelInterceptor to authorize messages based on the user header in them. Also,
    // Spring Session provides WebSocket integration that ensures the user’s HTTP session does not expire while the WebSocket session is still active.
}
