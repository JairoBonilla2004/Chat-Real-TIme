package ec.edu.espe.chat_real_time.repository;

import ec.edu.espe.chat_real_time.model.message.Message;
import ec.edu.espe.chat_real_time.model.message.MessageType;
import ec.edu.espe.chat_real_time.model.room.Room;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.model.user.UserSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
  List<Message> findByRoomOrderBySentAtDesc(Room room);
  List<Message> findByRoomAndIsDeletedFalseOrderBySentAtDesc(Room room);
  Page<Message> findByRoomAndIsDeletedFalseOrderBySentAtDesc(Room room, Pageable pageable);

  List<Message> findByUserAndIsDeletedFalse(User user);
  List<Message> findBySessionAndIsDeletedFalse(UserSession session);

  @Query("SELECT m FROM Message m WHERE m.room = :room AND m.isDeleted = false AND m.sentAt >= :since ORDER BY m.sentAt DESC")
  List<Message> findRecentMessages(Room room, LocalDateTime since);

  @Query("SELECT COUNT(m) FROM Message m WHERE m.room = :room AND m.messageType = :type")
  long countByRoomAndType(Room room, MessageType type);

  long countByRoom(Room room);
}
