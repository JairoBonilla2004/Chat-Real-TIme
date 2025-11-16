package ec.edu.espe.chat_real_time.Service;
import ec.edu.espe.chat_real_time.Service.auth.AuthServiceImpl;
import ec.edu.espe.chat_real_time.Service.refreshToken.RefreshTokenServiceImpl;
import ec.edu.espe.chat_real_time.Service.user.UserServiceImpl;
import ec.edu.espe.chat_real_time.dto.request.RegisterRequest;
import ec.edu.espe.chat_real_time.dto.response.RegisterResponse;
import ec.edu.espe.chat_real_time.model.Role;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.repository.GuestProfileRepository;
import ec.edu.espe.chat_real_time.repository.RoleRepository;
import ec.edu.espe.chat_real_time.repository.UserRepository;
import ec.edu.espe.chat_real_time.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private RefreshTokenServiceImpl refreshTokenService;
    private HttpRequestService httpRequestService;
    private UserServiceImpl userService;
    private RoleRepository roleRepository;
    private GuestProfileRepository guestProfileRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);
        refreshTokenService = mock(RefreshTokenServiceImpl.class);
        httpRequestService = mock(HttpRequestService.class);
        userService = mock(UserServiceImpl.class);
        roleRepository = mock(RoleRepository.class);
        guestProfileRepository = mock(GuestProfileRepository.class);
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        authService = new AuthServiceImpl(
                authenticationManager,
                jwtService,
                refreshTokenService,
                httpRequestService,
                userService,
                roleRepository,
                guestProfileRepository,
                userRepository,
                passwordEncoder
        );
    }

    @Test
    void registerAdmin_ShouldCreateAdminUserSuccessfully() {


        RegisterRequest request = new RegisterRequest();
        request.setUsername("admin123");
        request.setPassword("1234");
        request.setFirstName("Juan");
        request.setLastName("Lopez");
        request.setEmail("admin@mail.com");
        request.setPhone("0999999999");

        when(userRepository.existsByUsername("admin123")).thenReturn(false);


        when(passwordEncoder.encode("1234")).thenReturn("encoded-pass");


        Role role = new Role();
        role.setId(1L);
        role.setName("ROLE_ADMIN");
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(role));


        User savedUser = new User();
        savedUser.setId(10L);
        savedUser.setUsername("admin123");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);


        RegisterResponse response = authService.registerAdmin(request);


        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getUsername()).isEqualTo("admin123");
        assertThat(response.getRole()).isEqualTo("ROLE_ADMIN");


        verify(userRepository, times(1)).save(any(User.class));
    }
}
