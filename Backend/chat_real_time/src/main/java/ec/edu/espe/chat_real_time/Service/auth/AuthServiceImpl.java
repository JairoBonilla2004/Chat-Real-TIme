package ec.edu.espe.chat_real_time.Service.auth;

import ec.edu.espe.chat_real_time.Service.HttpRequestService;
import ec.edu.espe.chat_real_time.Service.refreshToken.RefreshTokenServiceImpl;
import ec.edu.espe.chat_real_time.Service.user.UserServiceImpl;
import ec.edu.espe.chat_real_time.dto.mapperDTO.UserMapper;
import ec.edu.espe.chat_real_time.dto.request.GuestLoginRequest;
import ec.edu.espe.chat_real_time.dto.request.LoginRequest;
import ec.edu.espe.chat_real_time.dto.request.RegisterRequest;
import ec.edu.espe.chat_real_time.dto.response.RegisterResponse;
import ec.edu.espe.chat_real_time.dto.response.AuthResponse;
import ec.edu.espe.chat_real_time.exception.InvalidTokenException;
import ec.edu.espe.chat_real_time.model.RefreshToken;
import ec.edu.espe.chat_real_time.model.Role;
import ec.edu.espe.chat_real_time.model.user.AdminProfile;
import ec.edu.espe.chat_real_time.model.user.GuestProfile;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.repository.GuestProfileRepository;
import ec.edu.espe.chat_real_time.repository.RoleRepository;
import ec.edu.espe.chat_real_time.security.jwt.JwtService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ec.edu.espe.chat_real_time.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
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
  private final GuestProfileRepository guestProfileRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;


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
      user.setIpAddress(httpRequestService.getClientIpAddress(httpServletRequest));
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
      userService.recordSuccessfulLogin(user.getUsername());
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
    String clientIp = httpRequestService.getClientIpAddress(httpRequest);
    Optional<GuestProfile> existingProfileOpt =
            guestProfileRepository.findByNickname(request.getNickname());

    if (existingProfileOpt.isPresent()) {
      GuestProfile existingProfile = existingProfileOpt.get();
      if (existingProfile.getExpiresAt().isBefore(LocalDateTime.now())) {
        userService.delete(existingProfile.getUser());
        return createNewGuestSession(request, clientIp);
      }

      User existingUser = existingProfile.getUser();
      String token = jwtService.generateAccessToken(existingUser);

      return AuthResponse.builder()
              .accessToken(token)
              .tokenType("Bearer")
              .expiresIn(jwtService.getAccessTokenExpiration())
              .guestInfo(UserMapper.toUserGuestResponse(existingProfile))
              .build();
    }

    return createNewGuestSession(request, clientIp);
  }

  private AuthResponse createNewGuestSession(GuestLoginRequest request, String clientIp) {

    String guestUsername =
            guestUsernamePrefix + UUID.randomUUID().toString().substring(0, 8);

    String guestNickname =
            request.getNickname() + "#" + UUID.randomUUID().toString().substring(0, 4);

    User guestUser = new User();
    guestUser.setUsername(guestUsername);
    guestUser.setIsActive(true);
    guestUser.setIpAddress(clientIp);

    Role guestRole = roleRepository.findByName("ROLE_GUEST")
            .orElseThrow(() -> new EntityNotFoundException("Rol 'ROLE_GUEST' no encontrado"));
    guestUser.getRoles().add(guestRole);

    GuestProfile guestProfile = new GuestProfile();
    guestProfile.setNickname(guestNickname);
    guestProfile.setExpiresAt(LocalDateTime.now().plusHours(guestSessionDurationHours));
    guestProfile.setUser(guestUser);

    guestUser.setGuestProfile(guestProfile);

    User savedUser = userService.saveUserDB(guestUser)
            .orElseThrow(() -> new IllegalStateException("Error al guardar el usuario invitado"));
    String token = jwtService.generateAccessToken(savedUser);
    return AuthResponse.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .expiresIn(jwtService.getAccessTokenExpiration())
            .guestInfo(UserMapper.toUserGuestResponse(savedUser.getGuestProfile()))
            .build();
  }


  @Override
  @Transactional
  public void logout(String refreshToken) {
    refreshTokenService.revokeToken(refreshToken);
  }

  @Override
  @Transactional
  public void logoutFromAllDevices(String refreshToken) {
    User user = refreshTokenService.findByToken(refreshToken).getUser();
    refreshTokenService.revokeAllUserTokens(user.getId());
  }

  @Transactional
  public AuthResponse refreshToken(String cookieValue, HttpServletRequest httpServletRequest) {
    if (cookieValue == null || cookieValue.isEmpty()) {
      throw new InvalidTokenException("Refresh token is missing");
    }
    RefreshToken refreshToken = refreshTokenService.findByToken(cookieValue);

    if (refreshToken.isExpired()) {
      refreshTokenService.deleteByToken(refreshToken.getToken());
      throw new InvalidTokenException("Refresh token has expired");
    }

    if (!refreshToken.getIsActive()) {
      throw new InvalidTokenException("Refresh token is no longer active");
    }
    User user = refreshToken.getUser();
    String newAccessToken = jwtService.generateAccessToken(user);
    RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken,
            httpRequestService.getClientIpAddress(httpServletRequest),
            httpRequestService.getUserAgent(httpServletRequest),
            httpRequestService.getDeviceInfo(httpServletRequest)
    );

    return AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken.getToken())
            .tokenType("Bearer")
            .expiresIn(jwtService.getAccessTokenExpiration())
            .userInfo(UserMapper.toUserAdminResponse(user.getAdminProfile()))
            .build();

  }
  @Override
  @Transactional
    public RegisterResponse registerAdmin (RegisterRequest request){
      if (userRepository.existsByUsername(request.getUsername())) {
          throw new IllegalArgumentException("El username ya existe");
      }
      User newUser = new User();
      newUser.setUsername(request.getUsername());

      newUser.setPassword(passwordEncoder.encode(request.getPassword()));
      newUser.setIsActive(true);

      Role adminRole = roleRepository.findByName("ROLE_ADMIN")
              .orElseThrow(() -> new RuntimeException("Role 'ROLE_ADMIN' no encontrado"));
      newUser.getRoles().add(adminRole);

      AdminProfile profile = new AdminProfile();
      profile.setFirstName(request.getFirstName());
      profile.setLastName(request.getLastName());
      profile.setEmail(request.getEmail());
      profile.setPhone(request.getPhone());
      profile.setUser(newUser);

      newUser.setAdminProfile(profile);

      User saved = userRepository.save(newUser);

      return RegisterResponse.builder()
              .id(saved.getId())
              .username(saved.getUsername())
              .role("ROL_ADMIN")
              .build();


  }



}
