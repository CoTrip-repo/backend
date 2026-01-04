package com.ssafy.cotrip.global.config;

import com.ssafy.cotrip.security.jwt.JwtTokenUtil;
import com.ssafy.cotrip.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class StompConfig implements WebSocketMessageBrokerConfigurer {

    private static final String WS_AUTH_ATTR = "WS_AUTH";

    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                StompCommand command = accessor.getCommand();
                if (command == null) return message;

                accessor.setLeaveMutable(true);

                if (StompCommand.CONNECT.equals(command)) {
                    Authentication auth = authenticateFromConnectHeaders(accessor);

                    accessor.setUser(auth);

                    Map<String, Object> attrs = accessor.getSessionAttributes();
                    if (attrs != null) attrs.put(WS_AUTH_ATTR, auth);

                    return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
                }

                if (StompCommand.SEND.equals(command)
                        || StompCommand.SUBSCRIBE.equals(command)
                        || StompCommand.UNSUBSCRIBE.equals(command)) {

                    Authentication auth = resolveAuth(accessor);
                    if (auth == null) {
                        if (accessor.getSessionAttributes() == null) {
                            return message;
                        }
                        throw new AccessDeniedException("Unauthorized STOMP message");
                    }

                    accessor.setUser(auth);

                    return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
                }

                // DISCONNECT는 인증 없이도 허용
                if (StompCommand.DISCONNECT.equals(command)) {
                    return message;
                }

                return message;
            }

            private Authentication authenticateFromConnectHeaders(StompHeaderAccessor accessor) {
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    throw new AccessDeniedException("Unauthorized STOMP connection");
                }

                String token = authHeader.substring(7);
                if (jwtTokenUtil.isTokenExpired(token)) {
                    throw new AccessDeniedException("Expired JWT token");
                }

                String email = jwtTokenUtil.extractUsername(token);
                var userDetails = userDetailsService.loadUserByUsername(email);

                return new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
            }

            private Authentication resolveAuth(StompHeaderAccessor accessor) {
                if (accessor.getUser() instanceof Authentication a) return a;

                Map<String, Object> attrs = accessor.getSessionAttributes();
                if (attrs == null) return null;

                Object authObj = attrs.get(WS_AUTH_ATTR);
                if (authObj instanceof Authentication a) return a;

                return null;
            }
        });
    }
}
