package ec.edu.espe.chat_real_time.controller;

import ec.edu.espe.chat_real_time.Service.auth.AuthService;
import ec.edu.espe.chat_real_time.dto.request.GuestLoginRequest;
import ec.edu.espe.chat_real_time.dto.request.LoginRequest;
import ec.edu.espe.chat_real_time.dto.request.RegisterAdminRequest;
import ec.edu.espe.chat_real_time.dto.response.ApiResponse;
import ec.edu.espe.chat_real_time.dto.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private final AuthService authService = mock(AuthService.class);
    private final HttpServletRequest httpRequest = mock(HttpServletRequest.class);
    private final HttpServletResponse httpResponse = mock(HttpServletResponse.class);

    private final AuthController controller = new AuthController(authService);

    private AuthResponse mockAuthResponse() {
        return new AuthResponse(
                "accessToken",
                "refreshToken",
                "username",
                999999L,
                null,
                null
        );
    }

    @Test
    void testLogin() {
        LoginRequest request = new LoginRequest("user@test.com", "123");

        AuthResponse authResponse = mockAuthResponse();
        when(authService.login(eq(request), any())).thenReturn(authResponse);

        ResponseEntity<ApiResponse<AuthResponse>> response =
                controller.login(request, httpRequest, httpResponse);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Login successful", response.getBody().getMessage());

        verify(httpResponse, times(1)).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    void testGuestLogin() {
        GuestLoginRequest request = new GuestLoginRequest("Invitado");

        AuthResponse authResponse = mockAuthResponse();
        when(authService.guestLogin(eq(request), any())).thenReturn(authResponse);

        ResponseEntity<ApiResponse<AuthResponse>> response =
                controller.guestLogin(request, httpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Acceso de invitado exitoso", response.getBody().getMessage());
    }

    @Test
    void testRefreshToken() {
        AuthResponse authResponse = mockAuthResponse();
        when(authService.refreshToken(eq("token123"), any())).thenReturn(authResponse);

        ResponseEntity<ApiResponse<AuthResponse>> response =
                controller.refreshToken("token123", httpRequest, httpResponse);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Token refreshed successfully", response.getBody().getMessage());

        verify(httpResponse, times(1)).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    void testLogout() {
        ResponseEntity<ApiResponse<String>> response =
                controller.logout("token123", httpRequest, httpResponse);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logged out successfully", response.getBody().getMessage());

        verify(authService).logout("token123");
        verify(httpResponse, times(1)).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    void testLogoutAll() {
        ResponseEntity<ApiResponse<String>> response =
                controller.logoutAll("token123", httpRequest, httpResponse);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logged out from all devices successfully", response.getBody().getMessage());

        verify(authService).logoutFromAllDevices("token123");
    }

    @Test
    void testRegister() {
        RegisterAdminRequest request = new RegisterAdminRequest();

        AuthResponse authResponse = mockAuthResponse();

        when(authService.registerAdmin(eq(request), any())).thenReturn(authResponse);

        ResponseEntity<ApiResponse<AuthResponse>> response =
                controller.registerAdmin(request, httpRequest, httpResponse);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Administrador registrado correctamente", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        verify(httpResponse, times(1)).addHeader(eq("Set-Cookie"), anyString());
    }

}
