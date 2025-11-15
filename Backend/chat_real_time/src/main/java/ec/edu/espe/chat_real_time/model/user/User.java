package ec.edu.espe.chat_real_time.model.user;

import ec.edu.espe.chat_real_time.model.RefreshToken;
import ec.edu.espe.chat_real_time.model.Role;
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
        @Index(name = "idx_users_username", columnList = "username"),
        @Index(name = "idx_users_is_active", columnList = "isActive"),
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

  @Column(name = "password", length = 255)
  private String password;

  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column (name = "locked_until")
  private LocalDateTime lockedUntil;

  //atributos de spring security
  @Column(name = "account_non_locked")
  @Builder.Default
  private Boolean accountNonLocked = true;

  @Column (name = "credentials_non_expired")
  @Builder.Default
  private Boolean credentialsNonExpired = true;

  @Column (name = "account_non_expired")
  @Builder.Default
  private Boolean accountNonExpired = true;

  @Column(name = "failed_login_attempts", nullable = false)
  @Builder.Default
  private Integer failedLoginAttempts = 0;

  @Column (name = "enabled")
  @Builder.Default
  private Boolean enabled = true;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
          name = "user_roles",
          joinColumns = @JoinColumn(name = "user_id"),
          inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

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

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private AdminProfile adminProfile;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private GuestProfile guestProfile;

  public boolean isAdmin() {
    return roles.stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
  }

  public boolean isGuest() {
    return roles.stream().anyMatch(role -> role.getName().equals("ROLE_GUEST"));
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<SimpleGrantedAuthority> authorities = new HashSet<>();
    String rolePrefix = "ROLE_";
    roles.forEach( role -> {
      String roleName = role.getName();
      if (!roleName.startsWith(rolePrefix)) {
        roleName = rolePrefix + roleName;
      }
      authorities.add(new SimpleGrantedAuthority(roleName));
    });
    return authorities;
  }

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

}