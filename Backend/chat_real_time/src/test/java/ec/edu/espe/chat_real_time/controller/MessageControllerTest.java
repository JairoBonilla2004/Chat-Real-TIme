package ec.edu.espe.chat_real_time.controller;

import ec.edu.espe.chat_real_time.Service.message.MessageService;
import ec.edu.espe.chat_real_time.dto.request.SendMessageRequest;
import ec.edu.espe.chat_real_time.dto.response.ApiResponse;
import ec.edu.espe.chat_real_time.dto.response.MessageResponse;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageControllerTest {

    private MessageService messageService;
    private UserRepository userRepository;
    private MessageController controller;

    private Authentication authentication;
    private User mockUser;

    @BeforeEach
    void setUp() {
        messageService = mock(MessageService.class);
        userRepository = mock(UserRepository.class);
        controller = new MessageController(messageService, userRepository);

        authentication = mock(Authentication.class);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("user@test.com");

        when(authentication.getName()).thenReturn("user@test.com");
        when(userRepository.findByUsernameAndDeletedAtIsNull("user@test.com"))
                .thenReturn(Optional.of(mockUser));
    }

    @Test
    void testSendTextMessage() {
        SendMessageRequest request = new SendMessageRequest();
        request.setRoomId(10L);
        request.setContent("Hola mundo");

        MessageResponse expectedResponse = new MessageResponse();
        when(messageService.sendTextMessage(eq(request), eq(mockUser)))
                .thenReturn(expectedResponse);

        ResponseEntity<ApiResponse<MessageResponse>> response =
                controller.sendTextMessage(request, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Mensaje enviado exitosamente", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());

        verify(messageService, times(1)).sendTextMessage(request, mockUser);
    }

    @Test
    void testSendFileMessage() {
        MultipartFile file = mock(MultipartFile.class);
        MessageResponse expectedResponse = new MessageResponse();

        when(messageService.sendFileMessage(eq(5L), eq("archivo"), eq(file), eq(mockUser)))
                .thenReturn(expectedResponse);

        ResponseEntity<ApiResponse<MessageResponse>> response =
                controller.sendFileMessage(5L, "archivo", file, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Archivo enviado exitosamente", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());

        verify(messageService, times(1))
                .sendFileMessage(5L, "archivo", file, mockUser);
    }

    @Test
    void testGetRoomMessages() {
        List<MessageResponse> mockList = List.of(new MessageResponse(), new MessageResponse());

        when(messageService.getRoomMessages(20L, mockUser)).thenReturn(mockList);

        ResponseEntity<ApiResponse<List<MessageResponse>>> response =
                controller.getRoomMessages(20L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Mensajes obtenidos exitosamente", response.getBody().getMessage());
        assertEquals(2, response.getBody().getData().size());

        verify(messageService).getRoomMessages(20L, mockUser);
    }

    @Test
    void testGetMessageById() {
        MessageResponse message = new MessageResponse();
        when(messageService.getMessageById(100L)).thenReturn(message);

        ResponseEntity<ApiResponse<MessageResponse>> response =
                controller.getMessageById(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Mensaje encontrado", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());

        verify(messageService).getMessageById(100L);
    }

    @Test
    void testDeleteMessage() {
        doNothing().when(messageService).deleteMessage(55L, mockUser);

        ResponseEntity<ApiResponse<Void>> response =
                controller.deleteMessage(55L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Mensaje eliminado exitosamente", response.getBody().getMessage());

        verify(messageService).deleteMessage(55L, mockUser);
    }

}
