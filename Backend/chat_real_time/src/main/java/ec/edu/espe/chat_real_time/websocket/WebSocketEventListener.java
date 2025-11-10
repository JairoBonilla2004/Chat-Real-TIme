package ec.edu.espe.chat_real_time.websocket;


import ec.edu.espe.chat_real_time.Service.device.DeviceSessionService;
import ec.edu.espe.chat_real_time.Service.websocket.WebSocketService;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

  private final WebSocketService webSocketService;
  private final UserRepository userRepository;
  private final DeviceSessionService deviceSessionService;

  @EventListener
  public void handleWebSocketConnectListener(SessionConnectedEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    Principal principal = headerAccessor.getUser();

    if (principal != null) {
      String username = principal.getName();
      log.info("WebSocket connection established for user: {}", username);

      User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
              .orElse(null);

      if (user != null) {
        webSocketService.notifyUserStatusChange(user, "ONLINE");
      }
    }
  }

  @EventListener
  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    Principal principal = headerAccessor.getUser();

    if (principal != null) {
      String username = principal.getName();
      log.info("WebSocket disconnected for user: {}", username);

      User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
              .orElse(null);

      if (user != null) {
        deviceSessionService.closeExistingSession(user);
        webSocketService.notifyUserStatusChange(user, "OFFLINE");
      }
    }
  }
}