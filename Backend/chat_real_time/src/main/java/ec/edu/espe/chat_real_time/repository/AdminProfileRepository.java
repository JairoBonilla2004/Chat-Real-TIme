package ec.edu.espe.chat_real_time.repository;

import org.springframework.stereotype.Repository;
import ec.edu.espe.chat_real_time.model.user.AdminProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

@Repository
public interface AdminProfileRepository extends JpaRepository<AdminProfile, Long> {
  Optional<AdminProfile> findByEmail(String email);
  boolean existsByEmail(String email);

  @Query("SELECT ap FROM AdminProfile ap WHERE ap.user.username = :username")
  Optional<AdminProfile> findByUsername(String username);
}