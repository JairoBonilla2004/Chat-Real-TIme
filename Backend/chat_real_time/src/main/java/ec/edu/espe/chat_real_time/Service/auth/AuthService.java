package ec.edu.espe.chat_real_time.Service.auth;

import ec.edu.espe.chat_real_time.dto.request.GuestLoginRequest;
import ec.edu.espe.chat_real_time.dto.request.LoginRequest;
import ec.edu.espe.chat_real_time.dto.request.RegisterAdminRequest;
import ec.edu.espe.chat_real_time.dto.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
  AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);
  AuthResponse guestLogin(GuestLoginRequest request, HttpServletRequest httpRequest);
  void logout(String refreshToken );
  void logoutFromAllDevices(String refreshToken);
  AuthResponse refreshToken(String cookieValue, HttpServletRequest httpServletRequest);
  AuthResponse registerAdmin(RegisterAdminRequest request, HttpServletRequest httpServletRequest);
}
