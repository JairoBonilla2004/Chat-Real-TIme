package ec.edu.espe.chat_real_time.controller;


import ec.edu.espe.chat_real_time.Service.auth.AuthService;
import ec.edu.espe.chat_real_time.dto.request.GuestLoginRequest;
import ec.edu.espe.chat_real_time.dto.request.LoginRequest;
import ec.edu.espe.chat_real_time.dto.response.ApiResponse;
import ec.edu.espe.chat_real_time.dto.response.AuthResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(
          @Valid @RequestBody LoginRequest request,
          HttpServletRequest httpServletRequest,
          HttpServletResponse httpServletResponse
  ) {
    AuthResponse authResponse = authService.login(request, httpServletRequest);
    Cookie cookie = new Cookie("refreshToken", authResponse.getRefreshToken());
    cookie.setHttpOnly(true);
    cookie.setPath("/api/v1/auth/"); // sirve para que solo envie la cookie a esta ruta
    cookie.setMaxAge(7 * 24 * 60 * 60);
    httpServletResponse.addCookie(cookie);
    authResponse.setRefreshToken(null);
    return ResponseEntity.status(HttpStatus.OK)
            .body(
                    ApiResponse.success("Login successful", authResponse)
            );
  }

  @PostMapping("/guest")
  public ResponseEntity<ApiResponse<AuthResponse>> guestLogin(
          @Valid @RequestBody GuestLoginRequest request,
          HttpServletRequest httpRequest
  ) {
    AuthResponse response = authService.guestLogin(request, httpRequest);
    return ResponseEntity.ok(ApiResponse.success("Acceso de invitado exitoso", response));
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
          @CookieValue("refreshToken") String refreshToken, //esta cookie viene del navegador automáticamente
          HttpServletRequest httpServletRequest,
          HttpServletResponse httpServletResponse
  ) {
    AuthResponse authResponse = authService.refreshToken(refreshToken, httpServletRequest);
    Cookie cookie = new Cookie("refreshToken", authResponse.getRefreshToken());
    cookie.setHttpOnly(true);
    cookie.setPath("/api/v1/auth/");
    cookie.setMaxAge(7 * 24 * 60 * 60);
    httpServletResponse.addCookie(cookie);
    authResponse.setRefreshToken(null);
    return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("Token refreshed successfully", authResponse));
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<String>> logout(
          @CookieValue(value = "refreshToken", required = false) String refreshToken, //el requi
          HttpServletResponse httpServletResponse
  ) {
    //log del refresh token para ver si existe
    log.info("Logout request received with refresh token: {}", refreshToken);
    if (refreshToken != null) {
      authService.logout(refreshToken);
      Cookie cookie = new Cookie("refreshToken", null);
      cookie.setHttpOnly(true);
      cookie.setPath("/api/v1/auth/");
      cookie.setMaxAge(0); // Eliminar la cookie
      httpServletResponse.addCookie(cookie);
    }
    return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("Logged out successfully", null));
  }

  @PostMapping("/logout-all")
  public ResponseEntity<ApiResponse<String>> logoutAll(
          @CookieValue(value = "refreshToken", required = false) String refreshToken,
          HttpServletResponse httpServletResponse
  ) {
    if (refreshToken != null) {
      authService.logoutFromAllDevices(refreshToken);
      Cookie cookie = new Cookie("refreshToken", null);
      cookie.setHttpOnly(true);
      cookie.setPath("/api/v1/auth/");
      cookie.setMaxAge(0); // Eliminar la cookie
      httpServletResponse.addCookie(cookie);
    }
    return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success("Logged out from all devices successfully", null));
  }


}