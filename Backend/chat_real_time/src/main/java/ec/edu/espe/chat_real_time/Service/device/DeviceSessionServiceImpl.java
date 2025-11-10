package ec.edu.espe.chat_real_time.Service.device;

import ec.edu.espe.chat_real_time.exception.BadRequestException;
import ec.edu.espe.chat_real_time.model.room.Room;
import ec.edu.espe.chat_real_time.model.user.User;


import ec.edu.espe.chat_real_time.model.user.UserSession;
import ec.edu.espe.chat_real_time.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceSessionServiceImpl implements DeviceSessionService {

  private final UserSessionRepository userSessionRepository;

  @Override
  @Transactional(readOnly = true)
  public void validateUniqueSession(User user, String deviceId, String ipAddress) {
    List<UserSession> allSessions = userSessionRepository.findByUserAndIsActiveTrue(user);

    List<UserSession> activeSessions = allSessions.stream()
            .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
            .toList();

    if (!activeSessions.isEmpty()) {
      boolean isSameDevice = activeSessions.stream()
              .anyMatch(session ->
                      session.getDeviceId().equals(deviceId) &&
                              session.getIpAddress().equals(ipAddress)
              );

      if (isSameDevice) {
        throw new BadRequestException(
                "Ya tienes una sesión activa en esta sala desde este dispositivo. " +
                        "Por favor, sal de la sala actual primero."
        );
      } else {
        throw new BadRequestException(
                "Ya existe una sesión activa en esta sala desde otro dispositivo o IP. " +
                        "Por favor, sal de la sesión anterior antes de ingresar."
        );
      }
    }
  }

  @Override
  @Transactional
  public void closeExistingSession(User user) {
    List<UserSession> activeSessions = userSessionRepository.findByUserAndIsActiveTrue(user);

    if (!activeSessions.isEmpty()) {
      activeSessions.forEach(session -> {
        session.setIsActive(false);
        session.setLeftAt(LocalDateTime.now());
        userSessionRepository.save(session);

        if (session.getRoom() != null) {
          session.getRoom().decrementCurrentUsers();
        }
      });
    }
  }

  @Override
  public String generateDeviceFingerprint(String userAgent, String ipAddress) {
    try {
      String rawFingerprint = userAgent + "|" + ipAddress + "|" + System.currentTimeMillis();
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(rawFingerprint.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash).substring(0, 32);
    } catch (NoSuchAlgorithmException e) {
      return "device_" + System.currentTimeMillis();
    }
  }
}