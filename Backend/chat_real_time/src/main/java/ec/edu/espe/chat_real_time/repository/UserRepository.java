package ec.edu.espe.chat_real_time.repository;

import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.model.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
  Optional<User> findByEmail(String email);
  Optional<User> findByUsernameAndDeletedAtIsNull(String username);
  Optional<User> findByEmailAndDeletedAtIsNull(String email);
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);
  boolean existsByUsernameAndDeletedAtIsNull(String username);

  @Query("SELECT u FROM User u WHERE u.isGuest = true AND u.guestExpiresAt < :now")
  List<User> findExpiredGuests(LocalDateTime now);

  @Modifying
  @Transactional
  @Query("DELETE FROM User u WHERE u.isGuest = true AND u.guestExpiresAt < :now")
  int deleteAllExpiredGuests(@Param("now") LocalDateTime now);

  List<User> findByRoleAndDeletedAtIsNull(UserRole role);
}