package ec.edu.espe.chat_real_time.model.user;

import ec.edu.espe.chat_real_time.model.RefreshToken;
import ec.edu.espe.chat_real_time.model.message.Message;
import ec.edu.espe.chat_real_time.model.room.Room;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_role", columnList = "role"),
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_is_guest", columnList = "isGuest"),
        @Index(name = "idx_users_deleted_at", columnList = "deletedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class User implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(unique = true, length = 100)
  private String email;

  @Column(name = "password_hash", length = 255)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private UserRole role = UserRole.GUEST;

  @Column(length = 50)
  private String nickname;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "is_guest")
  @Builder.Default
  private Boolean isGuest = false;

  @Column(name = "guest_expires_at")
  private LocalDateTime guestExpiresAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  // Relaciones

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private Set<RefreshToken> refreshTokens = new HashSet<>();

  @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
  @Builder.Default
  private Set<Room> createdRooms = new HashSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  @Builder.Default
  private Set<UserSession> sessions = new HashSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  @Builder.Default
  private Set<Message> messages = new HashSet<>();

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<SimpleGrantedAuthority> authorities = new HashSet<>();
    String roleName = this.role.name();
    if (!roleName.startsWith("ROLE_")) {
      roleName = "ROLE_" + roleName;
    }
    authorities.add(new SimpleGrantedAuthority(roleName));
    return authorities;
  }

  @Column(name = "locked_until")
  private LocalDateTime lockedUntil;

  //Sprin Security UserDetails attributes
  @Column(name = "enabled", nullable = false)
  @Builder.Default
  private Boolean enabled = true;

  @Column(name = "account_non_expired", nullable = false)
  @Builder.Default
  private Boolean accountNonExpired = true;

  @Column(name = "credentials_non_expired", nullable = false)
  @Builder.Default
  private Boolean credentialsNonExpired = true;

  @Column(name = "account_non_locked", nullable = false)
  @Builder.Default
  private Boolean accountNonLocked = true;


  @Override
  public boolean isAccountNonLocked() {
    if (lockedUntil == null) {
      return Boolean.TRUE.equals(this.accountNonLocked);
    }
    return LocalDateTime.now().isAfter(lockedUntil);
  }

  @Override
  public boolean isEnabled() {
    return Boolean.TRUE.equals(this.enabled);
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return Boolean.TRUE.equals(this.credentialsNonExpired);
  }

  @Override
  public boolean isAccountNonExpired() {
    return Boolean.TRUE.equals(this.accountNonExpired);
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }
}