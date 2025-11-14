package ec.edu.espe.chat_real_time.websocket;

import ec.edu.espe.chat_real_time.Service.websocket.WebSocketService;
import ec.edu.espe.chat_real_time.model.room.Room;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.model.user.UserSession;
import ec.edu.espe.chat_real_time.repository.RoomRepository;
import ec.edu.espe.chat_real_time.repository.UserRepository;
import ec.edu.espe.chat_real_time.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

  private final WebSocketService webSocketService;
  private final UserRepository userRepository;
  private final RoomSubscriptionTracker subscriptionTracker;
  private final UserSessionRepository userSessionRepository;
  private final RoomRepository roomRepository;

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
    String sessionId = headerAccessor.getSessionId();

    if (principal != null) {
      String username = principal.getName();
      log.info("WebSocket disconnected for user: {}", username);

      User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
              .orElse(null);

      if (user != null) {
        subscriptionTracker.getRoomId(sessionId).ifPresent(roomId -> {
          Room room = roomRepository.findByIdAndDeletedAtIsNull(roomId)
                  .orElse(null);
          if (room != null) {
            UserSession activeSession = userSessionRepository.findByUserAndRoomAndIsActiveTrue(user, room)
                    .orElse(null);
            if (activeSession != null) {
              activeSession.setIsActive(false);
              activeSession.setLeftAt(LocalDateTime.now());
              userSessionRepository.save(activeSession);

              room.decrementCurrentUsers();
              roomRepository.save(room);

              webSocketService.notifyUserLeftRoom(room.getId(), user);
            }
          }
          subscriptionTracker.unmap(sessionId);
        });

        webSocketService.notifyUserStatusChange(user, "OFFLINE");
      }
    }
  }
}