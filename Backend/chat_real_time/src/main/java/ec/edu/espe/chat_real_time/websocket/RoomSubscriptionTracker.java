package ec.edu.espe.chat_real_time.websocket;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomSubscriptionTracker {
  private final ConcurrentHashMap<String, Long> sessionToRoom = new ConcurrentHashMap<>();

  public void map(String sessionId, Long roomId) {
    if (sessionId != null && roomId != null) {
      sessionToRoom.put(sessionId, roomId);
    }
  }

  public Optional<Long> getRoomId(String sessionId) {
    if (sessionId == null) return Optional.empty();
    return Optional.ofNullable(sessionToRoom.get(sessionId));
  }

  public void unmap(String sessionId) {
    if (sessionId != null) sessionToRoom.remove(sessionId);
  }
}
