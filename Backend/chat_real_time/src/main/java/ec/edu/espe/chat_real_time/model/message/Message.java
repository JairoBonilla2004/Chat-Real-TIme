package ec.edu.espe.chat_real_time.model.message;


import ec.edu.espe.chat_real_time.model.Attachment;
import ec.edu.espe.chat_real_time.model.room.Room;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.model.user.UserSession;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_message_room", columnList = "room_id"),
        @Index(name = "idx_message_user", columnList = "user_id"),
        @Index(name = "idx_message_sent_at", columnList = "sentAt"),
        @Index(name = "idx_message_session", columnList = "session_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(name = "message_type", nullable = false)
  @Builder.Default
  private MessageType messageType = MessageType.TEXT;

  @CreationTimestamp
  @Column(name = "sent_at", nullable = false, updatable = false)
  private LocalDateTime sentAt;

  @Column(name = "is_edited")
  @Builder.Default
  private Boolean isEdited = false;

  @Column(name = "edited_at")
  private LocalDateTime editedAt;

  @Column(name = "is_deleted")
  @Builder.Default
  private Boolean isDeleted = false;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  private Room room;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id", nullable = false)
  private UserSession session;

  @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Attachment> attachments = new ArrayList<>();
}