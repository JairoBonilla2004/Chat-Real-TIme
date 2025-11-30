package ec.edu.espe.chat_real_time.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Cloudinary;


import ec.edu.espe.chat_real_time.Service.message.MessageServiceImpl;
import ec.edu.espe.chat_real_time.dto.request.SendMessageRequest;
import ec.edu.espe.chat_real_time.dto.response.MessageResponse;
import ec.edu.espe.chat_real_time.exception.BadRequestException;
import ec.edu.espe.chat_real_time.exception.ResourceNotFoundException;
import ec.edu.espe.chat_real_time.exception.UnauthorizedException;
import ec.edu.espe.chat_real_time.model.message.Message;
import ec.edu.espe.chat_real_time.model.message.MessageType;
import ec.edu.espe.chat_real_time.model.room.Room;
import ec.edu.espe.chat_real_time.model.room.RoomType;
import ec.edu.espe.chat_real_time.model.user.GuestProfile;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.model.user.UserSession;
import ec.edu.espe.chat_real_time.repository.AttachmentRepository;
import ec.edu.espe.chat_real_time.repository.MessageRepository;
import ec.edu.espe.chat_real_time.repository.RoomRepository;
import ec.edu.espe.chat_real_time.repository.UserSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageServiceImplTest {

    private MessageRepository messageRepository;
    private RoomRepository roomRepository;
    private UserSessionRepository sessionRepository;
    private AttachmentRepository attachmentRepository;
    private SimpMessagingTemplate messagingTemplate;
    private Cloudinary cloudinary;

    private MessageServiceImpl messageService;

    @BeforeEach
    void setUp() {
        messageRepository = mock(MessageRepository.class);
        roomRepository = mock(RoomRepository.class);
        sessionRepository = mock(UserSessionRepository.class);
        attachmentRepository = mock(AttachmentRepository.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        cloudinary = mock(Cloudinary.class);

        messageService = new MessageServiceImpl(
                messageRepository, roomRepository, sessionRepository,
                attachmentRepository, messagingTemplate, cloudinary
        );
    }

    @Test
    void sendTextMessage_ShouldSendMessage() {

        User user = new User();
        user.setUsername("testUser");

        GuestProfile guestProfile = new GuestProfile();
        guestProfile.setNickname("TestNickname");
        user.setGuestProfile(guestProfile); // <-- Esto evita el NullPointerException


        Room room = new Room();
        room.setId(1L);


        SendMessageRequest request = new SendMessageRequest();
        request.setRoomId(1L);
        request.setContent("Hello world");

        when(roomRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(room));

        UserSession session = new UserSession();
        when(sessionRepository.findByUserAndRoomAndIsActiveTrue(user, room))
                .thenReturn(Optional.of(session));


        Message savedMessage = Message.builder()
                .id(100L)
                .content("Hello world")
                .user(user)
                .room(room)
                .build();
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);


        MessageResponse response = messageService.sendTextMessage(request, user);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("Hello world");
        assertThat(response.getSenderNickname()).isEqualTo("TestNickname"); // Opcional: verificar nickname

        verify(messagingTemplate).convertAndSend(eq("/topic/room/1"), any(MessageResponse.class));
    }


    @Test
    void sendTextMessage_ShouldThrowIfRoomNotFound() {
        when(roomRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
        User user = new User();
        SendMessageRequest request = new SendMessageRequest();
        request.setRoomId(1L);
        request.setContent("Hello");
        assertThatThrownBy(() -> messageService.sendTextMessage(request, user))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getRoomMessages_ShouldThrowIfUserNotConnected() {
        User user = new User();
        Room room = new Room();
        room.setId(1L);
        when(roomRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(room));
        when(sessionRepository.findByUserAndRoomAndIsActiveTrue(user, room)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.getRoomMessages(1L, user))
                .isInstanceOf(UnauthorizedException.class);
    }



    @Test
    void getMessageById_ShouldReturnMessage() {

        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setRoles(new HashSet<>());


        GuestProfile guestProfile = new GuestProfile();
        guestProfile.setNickname("Guest#1234");
        user.setGuestProfile(guestProfile);


        Room room = new Room();
        room.setId(100L);


        Message message = Message.builder()
                .id(10L)
                .user(user)
                .room(room)
                .content("Hola mundo")
                .build();

      when(messageRepository.findById(10L)).thenReturn(Optional.of(message));

      MessageResponse response = messageService.getMessageById(10L);

       assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("Hola mundo");
        assertThat(response.getSenderId()).isEqualTo(1L);
        assertThat(response.getSenderNickname()).isEqualTo("Guest#1234");
        assertThat(response.getRoomId()).isEqualTo(100L);
    }


    @Test
    void deleteMessage_ShouldMarkDeleted() {
        GuestProfile guestProfile = new GuestProfile();
        guestProfile.setNickname("guest123");

        User user = new User();
        user.setId(1L);
        user.setGuestProfile(guestProfile);

        Room room = new Room();
        room.setId(100L);

        Message message = Message.builder()
                .id(10L)
                .user(user)
                .room(room)
                .build();

        when(messageRepository.findById(10L)).thenReturn(Optional.of(message));

        messageService.deleteMessage(10L, user);

        assertThat(message.getIsDeleted()).isTrue();
        assertThat(message.getDeletedAt()).isNotNull();

        verify(messageRepository).save(message);
        verify(messagingTemplate).convertAndSend(
                eq("/topic/room/" + room.getId()),
                any(MessageResponse.class)
        );
    }



    @Test
    void deleteMessage_ShouldThrowIfUserNotOwner() {
        User user = new User();
        user.setId(1L);
        User messageOwner = new User();
        messageOwner.setId(2L);

        Message message = Message.builder()
                .id(10L)
                .user(messageOwner)
                .room(new Room())
                .build();

        when(messageRepository.findById(10L)).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> messageService.deleteMessage(10L, user))
                .isInstanceOf(UnauthorizedException.class);
    }

}

