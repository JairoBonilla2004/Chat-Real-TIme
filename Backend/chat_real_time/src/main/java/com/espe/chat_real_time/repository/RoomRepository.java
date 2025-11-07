package com.espe.chat_real_time.repository;

import com.espe.chat_real_time.model.room.Room;
import com.espe.chat_real_time.model.room.RoomType;
import com.espe.chat_real_time.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
  Optional<Room> findByRoomCode(String roomCode);
  Optional<Room> findByRoomCodeAndDeletedAtIsNull(String roomCode);
  Optional<Room> findByIdAndDeletedAtIsNull(Long id);
  boolean existsByRoomCode(String roomCode);

  List<Room> findByCreatorAndDeletedAtIsNull(User creator);
  List<Room> findByIsActiveTrueAndDeletedAtIsNull();
  List<Room> findByTypeAndIsActiveTrueAndDeletedAtIsNull(RoomType type);

  @Query("SELECT r FROM Room r WHERE r.deletedAt IS NULL AND r.isActive = true ORDER BY r.createdAt DESC")
  List<Room> findAllActiveRooms();

  @Query("SELECT COUNT(r) FROM Room r WHERE r.creator = :creator AND r.deletedAt IS NULL")
  long countByCreator(User creator);
}