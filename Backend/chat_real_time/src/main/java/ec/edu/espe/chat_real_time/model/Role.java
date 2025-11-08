package ec.edu.espe.chat_real_time.model;


import ec.edu.espe.chat_real_time.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 20)
  private String name;

  @Column(length = 100)
  private String description;

  @ManyToMany(mappedBy = "roles")
  @Builder.Default
  private Set<User> users = new HashSet<>();


}