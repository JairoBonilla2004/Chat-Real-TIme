package ec.edu.espe.chat_real_time.Service.refreshToken;

import ec.edu.espe.chat_real_time.model.RefreshToken;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;

  @Value("${jwt.refresh-token.expiration}")
  private long refreshTokenExpiration;

  @Value("${jwt.refresh-token.max-per-user:5}")
  private int maxTokensPerUser;

  @Override
  public RefreshToken createRefreshToken(User user, String ipAddress, String userAgent, String deviceInfo) {
    deleteExpiredTokensByUser(user.getId());

    Optional<RefreshToken> existingTokenOpt =
            refreshTokenRepository.findActiveByUserIdAndUserAgent(user.getId(), userAgent);
    if (existingTokenOpt.isPresent()) {
      RefreshToken existToken = existingTokenOpt.get();
      if (!existToken.isExpired()) {
        return existToken;
      } else {
        existToken.revoke();
        refreshTokenRepository.save(existToken);
      }
    }
    limitTokenPerUser(user.getId());// esta parte sirve para limitar la cantidad de tokens activos por usuario antes de crear uno nuevo es decir si el usuario tiene 5 tokens activos y el maximo es 5 se revoca el mas antiguo
    RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .token(UUID.randomUUID().toString())
            .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .deviceInfo(deviceInfo)
            .isActive(true)
            .build();
    RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
    log.info("Created new refresh token for user {}", user.getId());
    return savedToken;
  }

  @Transactional
  public void deleteExpiredTokensByUser(Long userId) {
    List<RefreshToken> expiredTokens = refreshTokenRepository
            .findAllByUserIdAndExpiryDateBefore(userId, LocalDateTime.now());
    if (!expiredTokens.isEmpty()) {
      refreshTokenRepository.deleteAll(expiredTokens);
      log.info("Deleted {} expired refresh tokens for user {}", expiredTokens.size(), userId);
    }
  }

  @Transactional
  public void limitTokenPerUser(Long userId) {
    List<RefreshToken> activeTokens = refreshTokenRepository
            .findAllByUserIdAndIsActiveTrue(userId);
    if (activeTokens.size() >= maxTokensPerUser) {
      int tokensToRevoke = activeTokens.size() - maxTokensPerUser + 1;
      List<RefreshToken> tokensToRevokeList = activeTokens.stream()
              .sorted((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt())) // Ordenar por fecha de creación ascendente es decir de más antiguo a más reciente
              .limit(tokensToRevoke)
              .toList();
      tokensToRevokeList.forEach(token -> token.setIsActive(false));
      refreshTokenRepository.saveAll(tokensToRevokeList);
      log.info("Revoked {} refresh tokens for user {}", tokensToRevokeList.size(), userId);
    }
  }

}
