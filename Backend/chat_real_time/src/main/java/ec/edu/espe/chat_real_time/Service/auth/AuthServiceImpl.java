package ec.edu.espe.chat_real_time.Service.auth;

import ec.edu.espe.chat_real_time.Service.HttpRequestService;
import ec.edu.espe.chat_real_time.Service.refreshToken.RefreshTokenServiceImpl;
import ec.edu.espe.chat_real_time.Service.user.UserServiceImpl;
import ec.edu.espe.chat_real_time.dto.mapperDTO.UserMapper;
import ec.edu.espe.chat_real_time.dto.request.GuestLoginRequest;
import ec.edu.espe.chat_real_time.dto.request.LoginRequest;
import ec.edu.espe.chat_real_time.dto.response.AuthResponse;
import ec.edu.espe.chat_real_time.model.RefreshToken;
import ec.edu.espe.chat_real_time.model.Role;
import ec.edu.espe.chat_real_time.model.user.GuestProfile;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.repository.RoleRepository;
import ec.edu.espe.chat_real_time.security.jwt.JwtService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final RefreshTokenServiceImpl refreshTokenService;
  private final HttpRequestService httpRequestService;
  private final UserServiceImpl userService;
  private final RoleRepository roleRepository;

  @Value("${app.guest.session-duration-hours}")
  private int guestSessionDurationHours;

  @Value("${app.guest.username-prefix}")
  private String guestUsernamePrefix;

  @Override
  @Transactional
  public AuthResponse login(LoginRequest request, HttpServletRequest httpServletRequest) {
    try {
      Authentication authentication = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                      request.getUsername(),
                      request.getPassword()
              )
      );
      User user = (User) authentication.getPrincipal();
      if (!user.isAccountNonLocked()) {
        throw new LockedException("The admin is blocked");
      }

      if (!user.isEnabled()) {
        throw new DisabledException("The admin is not enabled");
      }

      if (user.getLockedUntil() != null && LocalDateTime.now().isAfter(user.getLockedUntil())) {// en caso de que la cuenta haya estado bloqueada y el tiempo de bloqueo haya pasado, se desbloquea la cuenta
        user.setLockedUntil(null);
        user.setAccountNonLocked(true);
        userService.saveUser(user);
      }

      String accessToken = jwtService.generateAccessToken(user);
      RefreshToken refreshToken = refreshTokenService.createRefreshToken(user
              , httpRequestService.getClientIpAddress(httpServletRequest)
              , httpRequestService.getUserAgent(httpServletRequest)
              , httpRequestService.getDeviceInfo(httpServletRequest)
      );
      return AuthResponse.builder()
              .accessToken(accessToken)
              .refreshToken(refreshToken.getToken())
              .tokenType("Bearer")
              .expiresIn(jwtService.getAccessTokenExpiration())
              .userInfo(UserMapper.toUserAdminResponse(user.getAdminProfile()))
              .build();

    } catch (BadCredentialsException e) {
      userService.recordFailedLoginAttempt(request.getUsername());
      throw new BadCredentialsException("Invalid username or password");
    } catch (LockedException e) {

      throw new LockedException("User account is locked");
    } catch (DisabledException e) {

      throw new DisabledException("User account is disabled");
    } catch (AuthenticationException e) {
      throw new AuthenticationServiceException("Authentication failed" + e.getMessage());
    }
  }

  @Override
  @Transactional
  public AuthResponse guestLogin(GuestLoginRequest request, HttpServletRequest httpRequest) {
    if (request.getNickname() == null || request.getNickname().isBlank()) {
      throw new IllegalArgumentException("El nickname no puede estar vacío");
    }

    String guestUsername = guestUsernamePrefix + UUID.randomUUID().toString().substring(0, 8);
    String clientIp = httpRequestService.getClientIpAddress(httpRequest);

    User guestUser = User.builder()
            .username(guestUsername)
            .isActive(true)
            .ipAddress(clientIp)
            .build();

    Role guestRole = roleRepository.findByName("ROLE_GUEST")
            .orElseThrow(() -> new EntityNotFoundException("Rol 'ROLE_GUEST' no encontrado"));
    guestUser.getRoles().add(guestRole);

    GuestProfile guestProfile = GuestProfile.builder()
            .nickname(request.getNickname())
            .expiresAt(LocalDateTime.now().plusHours(guestSessionDurationHours))
            .user(guestUser)
            .build();

    guestUser.setGuestProfile(guestProfile);

    User savedUser = userService.saveUserDB(guestUser)
            .orElseThrow(() -> new IllegalStateException("Error al guardar el usuario invitado"));

    String accessToken = jwtService.generateAccessToken(savedUser);
    return AuthResponse.builder()
            .accessToken(accessToken)
            .tokenType("Bearer")
            .expiresIn(jwtService.getAccessTokenExpiration())
            .guestInfo(UserMapper.toUserGuestResponse(savedUser.getGuestProfile()))
            .build();
  }


  @Override
  @Transactional
  public void logout(String refreshToken, HttpServletRequest httpServletRequest) {
    RefreshToken token = refreshTokenService.findByToken(refreshToken);
    refreshTokenService.revokeToken(refreshToken);

  }



}
