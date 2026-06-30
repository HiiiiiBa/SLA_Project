package com.sla.monitoring.websocket;

import com.sla.monitoring.security.jwt.JwtService;
import com.sla.monitoring.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Validates JWT tokens on STOMP CONNECT for secured WebSocket subscriptions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtStompChannelInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Rejected WebSocket CONNECT: missing Bearer token");
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        jwtService.validateAccessToken(token);
        String email = jwtService.extractEmail(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        accessor.setUser(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
