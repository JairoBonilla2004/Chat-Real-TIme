package ec.edu.espe.chat_real_time.websocket;

import ec.edu.espe.chat_real_time.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
      String token = accessor.getFirstNativeHeader("Authorization");

      if (token != null && token.startsWith("Bearer ")) {
        token = token.substring(7);

        try {
          String username = jwtService.extractUsername(token);
          UserDetails userDetails = userDetailsService.loadUserByUsername(username);

          if (username != null && jwtService.isTokenValid(token, userDetails)) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            accessor.setUser(authentication);

            log.info("WebSocket authenticated user: {}", username);
          }
        } catch (Exception e) {
          log.error("WebSocket authentication failed: {}", e.getMessage());
        }
      }
    }

    return message;
  }
}
