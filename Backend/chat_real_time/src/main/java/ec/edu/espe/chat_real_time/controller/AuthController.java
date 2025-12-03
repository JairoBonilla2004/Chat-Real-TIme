package ec.edu.espe.chat_real_time.controller;

import ec.edu.espe.chat_real_time.Service.auth.AuthService;
import ec.edu.espe.chat_real_time.dto.request.GuestLoginRequest;
import ec.edu.espe.chat_real_time.dto.request.LoginRequest;
import ec.edu.espe.chat_real_time.dto.request.RegisterAdminRequest;
import ec.edu.espe.chat_real_time.dto.response.ApiResponse;
import ec.edu.espe.chat_real_time.dto.response.AuthResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  private void setRefreshTokenCookie(HttpServletResponse response, String token) {

    ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
            .httpOnly(true)
            .secure(false)      // EN PRODUCCIÓN: cambiar a true (requiere HTTPS)
            .path("/")
            .sameSite("Lax")    // EN PRODUCCIÓN: cambiar a "None" si front está en otro dominio
            .maxAge(7 * 24 * 60 * 60)
            .build();

    response.addHeader("Set-Cookie", cookie.toString());
  }

  private void deleteRefreshTokenCookie(HttpServletResponse response) {

    ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(false)      //  PRODUCCIÓN: true
            .path("/")
            .sameSite("Lax")    // PRODUCCIÓN: "None" si usas dominios distintos
            .maxAge(0)
            .build();

    response.addHeader("Set-Cookie", cookie.toString());
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(
          @Valid @RequestBody LoginRequest request,
          HttpServletRequest httpRequest,
          HttpServletResponse httpResponse
  ) {

    AuthResponse authResponse = authService.login(request, httpRequest);
    setRefreshTokenCookie(httpResponse, authResponse.getRefreshToken());
    authResponse.setRefreshToken(null);

    return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
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
          @CookieValue("refreshToken") String refreshToken,
          HttpServletRequest httpRequest,
          HttpServletResponse httpResponse
  ) {

    AuthResponse authResponse = authService.refreshToken(refreshToken, httpRequest);
    setRefreshTokenCookie(httpResponse, authResponse.getRefreshToken());
    authResponse.setRefreshToken(null);

    return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", authResponse));
  }

  @PostMapping("/register-admin")
  public ResponseEntity<ApiResponse<AuthResponse>> registerAdmin(
          @Valid @RequestBody RegisterAdminRequest request,
          HttpServletRequest httpRequest,
          HttpServletResponse httpResponse
  ) {

    AuthResponse response = authService.registerAdmin(request, httpRequest);

    if (response.getRefreshToken() != null) {
      setRefreshTokenCookie(httpResponse, response.getRefreshToken());
      response.setRefreshToken(null);
    }

    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Administrador registrado correctamente", response));
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<String>> logout(
          @CookieValue(value = "refreshToken", required = false) String refreshToken,
          HttpServletRequest httpRequest,
          HttpServletResponse httpResponse
  ) {

    if (refreshToken != null) {
      authService.logout(refreshToken);
      deleteRefreshTokenCookie(httpResponse);
    }

    return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
  }

  @PostMapping("/logout-all")
  public ResponseEntity<ApiResponse<String>> logoutAll(
          @CookieValue(value = "refreshToken", required = false) String refreshToken,
          HttpServletRequest httpRequest,
          HttpServletResponse httpResponse
  ) {

    if (refreshToken != null) {
      authService.logoutFromAllDevices(refreshToken);
      deleteRefreshTokenCookie(httpResponse);
    }

    return ResponseEntity.ok(ApiResponse.success("Logged out from all devices successfully", null));
  }
}
