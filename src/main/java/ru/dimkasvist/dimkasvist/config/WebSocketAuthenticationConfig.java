package ru.dimkasvist.dimkasvist.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthenticationConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null && accessor.getSessionAttributes() != null) {
                    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                        UsernamePasswordAuthenticationToken authentication = 
                                (UsernamePasswordAuthenticationToken) accessor.getSessionAttributes().get("SPRING_SECURITY_CONTEXT");
                        
                        if (authentication != null) {
                            accessor.setUser(authentication);
                            accessor.getSessionAttributes().put("USER_AUTH", authentication);
                            log.info("Stored USER_AUTH in session for user: {}", authentication.getPrincipal());
                        }
                    } else {
                        UsernamePasswordAuthenticationToken authentication = 
                                (UsernamePasswordAuthenticationToken) accessor.getSessionAttributes().get("USER_AUTH");
                        
                        if (authentication != null) {
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            log.debug("Set SecurityContext for message from user: {}", authentication.getPrincipal());
                        } else {
                            log.warn("USER_AUTH not found in session attributes for command: {}", accessor.getCommand());
                        }
                    }
                }
                
                return message;
            }
        });
    }
}
