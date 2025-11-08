package ec.edu.espe.chat_real_time.model.user;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;


@Entity
@Table(name = "guest_profiles", indexes = {
        @Index(name = "idx_guest_nickname", columnList = "nickname"),
        @Index(name = "idx_guest_expires", columnList = "expiresAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestProfile {
  @Id
  private Long id;

  @Column(nullable = false, length = 50)
  private String nickname;

  @Column(name = "expires_at")
  private LocalDateTime expiresAt;

  @CreationTimestamp
  @Column(name = "joined_at", nullable = false, updatable = false)
  private LocalDateTime joinedAt;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "id")
  private User user;

  public boolean isExpired() {
    return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
  }

  public long getHoursUntilExpiration() {
    if (expiresAt == null) return 0;
    return java.time.Duration.between(LocalDateTime.now(), expiresAt).toHours();
  }
}