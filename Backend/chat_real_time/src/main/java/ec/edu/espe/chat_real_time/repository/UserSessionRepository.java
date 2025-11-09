package ec.edu.espe.chat_real_time.repository;


import ec.edu.espe.chat_real_time.model.room.Room;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.model.user.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
  Optional<UserSession> findByUserAndRoomAndIsActiveTrue(User user, Room room);
  Optional<UserSession> findByDeviceIdAndRoomAndIsActiveTrue(String deviceId, Room room);

  List<UserSession> findByRoomAndIsActiveTrue(Room room);
  List<UserSession> findByUserAndIsActiveTrue(User user);

  @Query("SELECT s FROM UserSession s WHERE s.nicknameInRoom = :nickname AND s.room = :room AND s.isActive = true")
  Optional<UserSession> findActiveSessionByNicknameAndRoom(String nickname, Room room); //

  @Query("SELECT COUNT(s) FROM UserSession s WHERE s.room = :room AND s.isActive = true")
  long countActiveSessionsByRoom(Room room);

  boolean existsByNicknameInRoomAndRoomAndIsActiveTrue(String nickname, Room room);
  boolean existsByUserAndIsActiveTrue(User user);
}