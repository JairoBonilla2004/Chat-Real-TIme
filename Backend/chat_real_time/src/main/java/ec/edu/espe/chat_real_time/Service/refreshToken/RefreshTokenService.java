package ec.edu.espe.chat_real_time.Service.refreshToken;

import ec.edu.espe.chat_real_time.model.RefreshToken;
import ec.edu.espe.chat_real_time.model.user.User;

public interface RefreshTokenService {
  
   RefreshToken createRefreshToken(User user, String ipAddress, String userAgent, String deviceInfo);

}
