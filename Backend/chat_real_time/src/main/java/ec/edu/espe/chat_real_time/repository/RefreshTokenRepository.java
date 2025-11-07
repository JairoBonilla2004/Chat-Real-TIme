package ec.edu.espe.chat_real_time.repository;

import ec.edu.espe.chat_real_time.model.RefreshToken;
import ec.edu.espe.chat_real_time.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByToken(String token);

  List<RefreshToken> findAllByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);

  List<RefreshToken> findAllByUserIdAndIsActiveTrue(Long userId);

  List<RefreshToken> findAllByUserIdAndExpiryDateBefore(Long userId, LocalDateTime dateTime);

  List<RefreshToken> findAllByExpiryDateBeforeAndIsActiveTrue(LocalDateTime dateTime);

  long countByUserIdAndIsActiveTrue(Long userId);

  long countByIsActiveTrue();

  long countByExpiryDateBeforeAndIsActiveTrue(LocalDateTime dateTime);

  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.token = :token")
  void deleteByToken(@Param("token") String token);

  @Modifying
  @Query("UPDATE RefreshToken rt SET rt.isActive = false WHERE rt.user.id = :userId AND rt.isActive = true")
  void revokeAllUserTokens(@Param("userId") Long userId);

  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
  void deleteAllByUserId(@Param("userId") Long userId);


  @Query("SELECT rt FROM RefreshToken rt " +
          "WHERE rt.user.id = :userId " +
          "AND rt.userAgent = :userAgent " +
          "AND rt.isActive = true")
  Optional<RefreshToken> findActiveByUserIdAndUserAgent(
          @Param("userId") Long userId,
          @Param("userAgent") String userAgent
  );
}