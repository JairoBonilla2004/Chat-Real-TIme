package ec.edu.espe.chat_real_time.controller;

import ec.edu.espe.chat_real_time.Service.room.RoomService;
import ec.edu.espe.chat_real_time.dto.request.CreateRoomRequest;
import ec.edu.espe.chat_real_time.dto.request.JoinRoomRequest;
import ec.edu.espe.chat_real_time.dto.response.ApiResponse;
import ec.edu.espe.chat_real_time.dto.response.RoomDetailResponse;
import ec.edu.espe.chat_real_time.dto.response.RoomResponse;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.repository.UserRepository;
import ec.edu.espe.chat_real_time.security.jwt.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomControllerTest {

    private RoomService roomService;
    private UserRepository userRepository;
    private JwtService jwtService;

    private RoomController controller;

    private Authentication authentication;
    private User mockUser;

    @BeforeEach
    void setUp() {
        roomService = mock(RoomService.class);
        userRepository = mock(UserRepository.class);
        jwtService = mock(JwtService.class);

        controller = new RoomController(roomService, userRepository, jwtService);

        authentication = mock(Authentication.class);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("admin@test.com");

        when(authentication.getName()).thenReturn("admin@test.com");
        when(userRepository.findByUsernameAndDeletedAtIsNull("admin@test.com"))
                .thenReturn(Optional.of(mockUser));
    }

    @Test
    void testCreateRoom() {
        CreateRoomRequest request = new CreateRoomRequest();
        RoomResponse roomResponse = new RoomResponse();

        when(roomService.createRoom(request, mockUser)).thenReturn(roomResponse);

        ResponseEntity<ApiResponse<RoomResponse>> response =
                controller.createRoom(request, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Sala creada exitosamente", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());

        verify(roomService).createRoom(request, mockUser);
    }

    @Test
    void testJoinRoom() {
        JoinRoomRequest request = new JoinRoomRequest();
        HttpServletRequest httpReq = mock(HttpServletRequest.class);

        RoomDetailResponse detail = new RoomDetailResponse();
        when(roomService.joinRoom(request, httpReq)).thenReturn(detail);

        ResponseEntity<ApiResponse<RoomDetailResponse>> response =
                controller.joinRoom(request, httpReq);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Te has unido a la sala exitosamente", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());

        verify(roomService).joinRoom(request, httpReq);
    }


    @Test
    void testLeaveRoom() {
        doNothing().when(roomService).leaveRoom(5L, mockUser);

        ResponseEntity<ApiResponse<Void>> response =
                controller.leaveRoom(5L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Has salido de la sala exitosamente", response.getBody().getMessage());

        verify(roomService).leaveRoom(5L, mockUser);
    }


    @Test
    void testGetAllActiveRooms() {
        List<RoomResponse> rooms = List.of(new RoomResponse(), new RoomResponse());

        when(roomService.getAllActiveRooms()).thenReturn(rooms);

        ResponseEntity<ApiResponse<List<RoomResponse>>> response =
                controller.getAllActiveRooms();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Salas obtenidas exitosamente", response.getBody().getMessage());
        assertEquals(2, response.getBody().getData().size());

        verify(roomService).getAllActiveRooms();
    }


    @Test
    void testGetRoomByCode() {
        RoomResponse room = new RoomResponse();
        when(roomService.getRoomByCode("ABC123")).thenReturn(room);

        ResponseEntity<ApiResponse<RoomResponse>> response =
                controller.getRoomByCode("ABC123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Sala encontrada", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());

        verify(roomService).getRoomByCode("ABC123");
    }


    @Test
    void testGetRoomDetails() {
        RoomDetailResponse detail = new RoomDetailResponse();
        when(roomService.getRoomDetails(10L)).thenReturn(detail);

        ResponseEntity<ApiResponse<RoomDetailResponse>> response =
                controller.getRoomDetails(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Detalles de sala obtenidos", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());

        verify(roomService).getRoomDetails(10L);
    }


    @Test
    void testGetMyRooms() {
        List<RoomResponse> rooms = List.of(new RoomResponse());

        when(roomService.getUserCreatedRooms(mockUser)).thenReturn(rooms);

        ResponseEntity<ApiResponse<List<RoomResponse>>> response =
                controller.getMyRooms(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Tus salas obtenidas exitosamente", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().size());

        verify(roomService).getUserCreatedRooms(mockUser);
    }

}
