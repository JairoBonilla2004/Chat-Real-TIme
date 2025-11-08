package ec.edu.espe.chat_real_time.repository;
import  ec.edu.espe.chat_real_time.model.user.GuestProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuestProfileRepository extends JpaRepository<GuestProfile, Long> {
  Optional<GuestProfile> findByNickname(String nickname);

  @Query("SELECT gp FROM GuestProfile gp WHERE gp.expiresAt < :now")
  List<GuestProfile> findExpiredProfiles(LocalDateTime now);

  @Query("SELECT gp FROM GuestProfile gp WHERE gp.user.username = :username")
  Optional<GuestProfile> findByUsername(String username);
}