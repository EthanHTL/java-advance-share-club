package club.smileboy.websocket.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurationSupport;

/**
 * 这是一个示例配置(sockJS SERVER)
 */
@Configuration
public class WebSocketSockJsServerConfig extends WebSocketMessageBrokerConfigurationSupport {

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/sockjs").withSockJS()

				// 这些含义还需要慢慢理解
			.setStreamBytesLimit(512 * 1024)
			.setHttpMessageCacheSize(1000)
			.setDisconnectDelay(30 * 1000);
	}

	// ...
}