package ec.edu.espe.chat_real_time.model.user;

import ec.edu.espe.chat_real_time.model.message.Message;
import ec.edu.espe.chat_real_time.model.room.Room;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_sessions",
        indexes = {
                @Index(name = "idx_session_room", columnList = "room_id"),
                @Index(name = "idx_session_active", columnList = "isActive")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uniq_nickname_per_room",
                        columnNames = {"room_id", "nicknameInRoom", "isActive"}
                ),
                @UniqueConstraint(
                        name = "uniq_user_room_device",
                        columnNames = {"user_id", "room_id", "deviceId", "isActive"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession { // esta clase nos permite manejar multiples sesiones por usuario es decir un usuario puede estar en la misma sala desde diferentes dispositivos
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @CreationTimestamp
  @Column(name = "joined_at", nullable = false, updatable = false)
  private LocalDateTime joinedAt;

  @Column(name = "left_at")
  private LocalDateTime leftAt;

  @Column(name = "device_id", nullable = false, length = 100)
  private String deviceId;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(name = "user_agent", length = 500)
  private String userAgent;

  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  // Relaciones

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  private Room room;

  @OneToMany(mappedBy = "session", cascade = CascadeType.ALL) //esta relacion nos pemite obtener todos los mensajes enviados desde esta sesion
  @Builder.Default
  private Set<Message> messages = new HashSet<>();
}
