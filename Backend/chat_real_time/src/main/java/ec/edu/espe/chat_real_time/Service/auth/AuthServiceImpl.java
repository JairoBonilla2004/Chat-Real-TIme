package ec.edu.espe.chat_real_time.Service.auth;

import ec.edu.espe.chat_real_time.Service.HttpRequestService;
import ec.edu.espe.chat_real_time.Service.refreshToken.RefreshTokenService;
import ec.edu.espe.chat_real_time.dto.mapperDTO.UserMapper;
import ec.edu.espe.chat_real_time.dto.request.GuestLoginRequest;
import ec.edu.espe.chat_real_time.dto.request.LoginRequest;
import ec.edu.espe.chat_real_time.dto.response.AuthResponse;
import ec.edu.espe.chat_real_time.model.RefreshToken;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.security.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;
  private final HttpRequestService httpRequestService;

  @Override
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
              .userInfo(UserMapper.toUserAdminResponse(user))
              .build();

    } catch (BadCredentialsException e) {
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
  public AuthResponse guestLogin(GuestLoginRequest request, HttpServletRequest httpRequest) {
    return null;
  }

  @Override
  public void logout(String token) {

  }


}
