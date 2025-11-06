package com.espe.chat_real_time.model.user;

import com.espe.chat_real_time.model.room.Room;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions",
        indexes = {
                @Index(name = "idx_session_ip_room", columnList = "ip_address, room_id"),
                @Index(name = "idx_session_user", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"ip_address", "room_id"})
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession { // esta clase sirve para manjar sesiones de usuarios, tanto reistrados como invitados

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String nickname;

  @Column(name = "ip_address", nullable = false, length = 45)
  private String ipAddress;

  @Column(name = "session_id", unique = true, nullable = false)
  private String sessionId;

  @Column(name = "is_active")
  private boolean isActive = true;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "last_activity")
  private LocalDateTime lastActivity;

  // Relaciones
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  private Room room;
}