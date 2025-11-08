package ec.edu.espe.chat_real_time.model.user;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_profiles", indexes = {
        @Index(name = "idx_admin_email", columnList = "email"),
        @Index(name = "idx_admin_phone", columnList = "phone")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProfile {
  @Id
  private Long id;

  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @Column(nullable = false, unique = true, length = 100)
  private String email;

  @Column(length = 20)
  private String phone;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "id")
  private User user;

  public String getFullName() {
    return firstName + " " + lastName;
  }
}