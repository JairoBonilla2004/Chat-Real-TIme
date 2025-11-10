package ec.edu.espe.chat_real_time.Service.device;

import ec.edu.espe.chat_real_time.model.room.Room;
import ec.edu.espe.chat_real_time.model.user.User;

public interface DeviceSessionService {
  void validateUniqueSession(User user, String deviceId, String ipAddress);
  void closeExistingSession(User user);
  String generateDeviceFingerprint(String userAgent, String ipAddress);
}
