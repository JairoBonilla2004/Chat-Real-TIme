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

  // Primero elimina las relaciones en user_roles para usuarios con guest profiles expirados
  @Modifying
  @Transactional
  @Query(value = "DELETE ur FROM user_roles ur " +
          "INNER JOIN users u ON ur.user_id = u.id " +
          "INNER JOIN guest_profiles gp ON u.id = gp.id " +
          "WHERE gp.expires_at < :now", nativeQuery = true)
  void deleteExpiredGuestRoles(@Param("now") LocalDateTime now);

  // Elimina los guest profiles expirados
  @Modifying
  @Transactional
  @Query(value = "DELETE gp FROM guest_profiles gp " +
          "WHERE gp.expires_at < :now", nativeQuery = true)
  void deleteExpiredGuestProfiles(@Param("now") LocalDateTime now);

  // Finalmente elimina los usuarios que ya no tienen guest profile (huérfanos)
  @Modifying
  @Transactional
  @Query(value = "DELETE u FROM users u " +
          "WHERE u.id NOT IN (SELECT id FROM guest_profiles) " +
          "AND u.id NOT IN (SELECT id FROM admin_profiles) " +
          "AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id)", nativeQuery = true)
  int deleteOrphanUsers();

  // Método principal que orquesta la eliminación
  @Transactional
  default int deleteAllExpiredGuests(LocalDateTime now) {
    deleteExpiredGuestRoles(now);
    deleteExpiredGuestProfiles(now);
    return deleteOrphanUsers();
  }
}
