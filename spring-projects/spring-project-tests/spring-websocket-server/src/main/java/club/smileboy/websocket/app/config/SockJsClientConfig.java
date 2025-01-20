package club.smileboy.websocket.app.config;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.sockjs.client.JettyXhrTransport;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import sun.net.www.http.HttpClient;

import java.util.Arrays;

/**
 * @author jasonj
 * @date 2024/6/20
 * @time 22:14
 * @description 这里其实是对sockJS Stack的java 客户端的配置
 *
 * 1.  The SockJS Java client supports the websocket, xhr-streaming, and xhr-polling transports.
 *   配置java 客户端的传输类型(websocket的传输类型分为很多种 ,jsr356 / jetty /eclise的相关实现)
 *   // 我还可以配置 xhr 的传输类型
 **/
@Configuration
public class SockJsClientConfig {

    @Bean
    public SockJsClient sockJsClient() {
        SockJsClient sockJsClient = new SockJsClient(Arrays.asList(
                // 配置一个传输,
                // 可以使用jsr356 运行时的 标准 websocketClient
                // 客户端那webSocketContainer 是通过  ContainerProvider

                new WebSocketTransport(new StandardWebSocketClient(ContainerProvider.getWebSocketContainer()))

                // JettyWebSocketClient
                // JettyWebSocketClient by using the Jetty 9+ native WebSocket API. 需要相关的依赖

                // 或者spring 对webSocketClient的其他实现
                // Any implementation of Spring’s WebSocketClient.
                ,

                // xhr 支持 流化 或者 轮询(pooling)
                new RestTemplateXhrTransport(new RestTemplate())

                // JettyXhrTransport uses Jetty’s HttpClient for HTTP requests.
//                new JettyXhrTransport(new org.eclipse.jetty.client.HttpClient())
        ));
        return sockJsClient;
    }

    // 消息的格式化
    // SockJS 使用 JSON 格式的数组来存储消息。默认情况下，使用 Jackson 2 并且需要位于类路径上。或者，您可以配置 SockJsMessageCodec 的自定义实现并在 SockJsClient 上配置它。

    // 为了模拟大量并发用户
    // HttpClient jettyHttpClient = new HttpClient();
    //jettyHttpClient.setMaxConnectionsPerDestination(1000);
    //jettyHttpClient.setExecutor(new QueuedThreadPool(1000));
}
