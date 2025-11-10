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

  List<UserSession> findByRoomAndIsActiveTrue(Room room);
  List<UserSession> findByUserAndIsActiveTrue(User user);



}