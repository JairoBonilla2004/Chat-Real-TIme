package ec.edu.espe.chat_real_time.repository;

import ec.edu.espe.chat_real_time.model.user.User;
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

  Optional<User> findByUsernameAndDeletedAtIsNull(String username);

  boolean existsByUsername(String username);

  boolean existsByUsernameAndDeletedAtIsNull(String username);


  @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_ADMIN' AND u.deletedAt IS NULL")
  List<User> findAllAdmins();

  @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_GUEST' AND u.deletedAt IS NULL")
  List<User> findAllGuests();

  @Query("SELECT u FROM User u JOIN u.roles r JOIN u.guestProfile gp " +
          "WHERE r.name = 'ROLE_GUEST' AND gp.expiresAt < CURRENT_TIMESTAMP AND u.isActive = true")
  List<User> findExpiredGuests();

  @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u JOIN u.roles r " +
          "WHERE u.id = :userId AND r.name = :roleName")
  boolean userHasRole(@Param("userId") Long userId, @Param("roleName") String roleName);

  @Modifying
  @Transactional
  @Query("""
              DELETE FROM User u
              WHERE u.id IN (
                  SELECT g.user.id
                  FROM GuestProfile g
                  WHERE g.expiresAt < :now
              )
          """)
  int deleteAllExpiredGuests(LocalDateTime now);
}
