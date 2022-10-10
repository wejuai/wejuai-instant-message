package com.wejuai.message.config;

import com.wejuai.message.controller.MessageController;
import com.wejuai.message.repository.AccountsRepository;
import com.wejuai.message.repository.ChatUserRecordRepository;
import com.wejuai.message.repository.SendMessageRepository;
import com.wejuai.message.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * @author ZM.Wang
 */

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final static Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    private final SendMessageRepository sendMessageRepository;
    private final UserRepository userRepository;
    private final ChatUserRecordRepository chatUserRecordRepository;
    private final AccountsRepository accountsRepository;

    public WebSocketConfig(SendMessageRepository sendMessageRepository, UserRepository userRepository, ChatUserRecordRepository chatUserRecordRepository, AccountsRepository accountsRepository) {
        this.sendMessageRepository = sendMessageRepository;
        this.userRepository = userRepository;
        this.chatUserRecordRepository = chatUserRecordRepository;
        this.accountsRepository = accountsRepository;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        logger.info("webSocket初始化");
        registry.addHandler(new MessageController(sendMessageRepository, userRepository, chatUserRecordRepository, accountsRepository),
                "/webSocket/**").setAllowedOrigins("*");
    }

}
