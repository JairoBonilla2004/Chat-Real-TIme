package com.espe.chat_real_time.model.room;


import com.espe.chat_real_time.model.message.Message;
import com.espe.chat_real_time.model.user.User;
import com.espe.chat_real_time.model.user.UserSession;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rooms", indexes = {
        @Index(name = "idx_room_code", columnList = "room_code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "room_code", unique = true, nullable = false, length = 20)
  private String roomCode;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 500)
  private String description;

  @Column(nullable = false, length = 255)
  private String pin;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RoomType type;

  @Column(name = "max_users")
  private Integer maxUsers = 50;

  @Column(name = "is_active")
  private boolean isActive = true;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @ElementCollection
  @CollectionTable(
          name = "room_connected_users",
          joinColumns = @JoinColumn(name = "room_id")
  )
  @Column(name = "nickname")
  @Builder.Default
  private Set<String> connectedNicknames = new HashSet<>();

  //Relaciones
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "creator_id", nullable = false)
  private User creator;

  @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private Set<Message> messages = new HashSet<>();

  @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // esta relacion permite saber las sesiones activas en una sala
  private Set<UserSession> userSessions = new HashSet<>();

  public boolean isFull() {
    return connectedNicknames.size() >= maxUsers;
  }

  public boolean addUser(String nickname) {
    if (isFull()) {
      return false;
    }
    return connectedNicknames.add(nickname);
  }

  public boolean removeUser(String nickname) {
    return connectedNicknames.remove(nickname);
  }
}