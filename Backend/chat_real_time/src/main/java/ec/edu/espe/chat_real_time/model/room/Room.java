package ec.edu.espe.chat_real_time.model.room;

import ec.edu.espe.chat_real_time.model.message.Message;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.model.user.UserSession;
import jakarta.persistence.*;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rooms", indexes = {
        @Index(name = "idx_room_code", columnList = "roomCode"),
        @Index(name = "idx_room_creator", columnList = "creator_id"),
        @Index(name = "idx_room_deleted_at", columnList = "deletedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "room_code", nullable = false, unique = true, length = 20)
  private String roomCode;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 500)
  private String description;

  @Column(name = "pin_hash", nullable = false, length = 255)
  private String pinHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RoomType type = RoomType.TEXT;

  @Column(name = "max_users")

  private Integer maxUsers;

  @Column(name = "current_users")
  @Builder.Default
  private Integer currentUsers = 0;

  @Column(name = "max_file_size_mb")

  private Integer maxFileSizeMb;

  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  //Relaciones

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "creator_id", nullable = false)
  private User creator;

  @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private Set<UserSession> sessions = new HashSet<>();

  @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private Set<Message> messages = new HashSet<>();

  public boolean isFull() {
    return currentUsers >= maxUsers;
  }

  public void incrementCurrentUsers() {
    this.currentUsers++;
  }

  public void decrementCurrentUsers() {
    if (this.currentUsers > 0) {
      this.currentUsers--;
    }
  }
}