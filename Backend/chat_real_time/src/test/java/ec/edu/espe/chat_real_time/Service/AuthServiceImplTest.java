package ec.edu.espe.chat_real_time.Service;
import ec.edu.espe.chat_real_time.Service.auth.AuthServiceImpl;
import ec.edu.espe.chat_real_time.Service.refreshToken.RefreshTokenServiceImpl;
import ec.edu.espe.chat_real_time.Service.user.UserServiceImpl;
import ec.edu.espe.chat_real_time.dto.request.RegisterAdminRequest;
import ec.edu.espe.chat_real_time.dto.response.AuthResponse;
import ec.edu.espe.chat_real_time.model.Role;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.repository.GuestProfileRepository;
import ec.edu.espe.chat_real_time.repository.RoleRepository;
import ec.edu.espe.chat_real_time.repository.UserRepository;
import ec.edu.espe.chat_real_time.repository.AdminProfileRepository;
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
    private AdminProfileRepository adminProfileRepository;
    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;

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
        adminProfileRepository = mock(AdminProfileRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userRepository = mock(UserRepository.class);

        authService = new AuthServiceImpl(
                authenticationManager,
                jwtService,
                refreshTokenService,
                httpRequestService,
                userService,
                roleRepository,
                guestProfileRepository,
                adminProfileRepository,
                passwordEncoder,
                userRepository
        );
    }

    @Test
    void registerAdmin_ShouldCreateAdminUserSuccessfully() {

        RegisterAdminRequest request = new RegisterAdminRequest();
        request.setUsername("admin123");
        request.setPassword("1234");
        request.setFirstName("Juan");
        request.setLastName("Lopez");
        request.setEmail("admin@mail.com");
        request.setPhone("0999999999");

        when(userRepository.existsByUsername("admin123")).thenReturn(false);
        when(adminProfileRepository.existsByEmail("admin@mail.com")).thenReturn(false);

        when(passwordEncoder.encode("1234")).thenReturn("encoded-pass");

        final Role role = new Role();
        role.setId(1L);
        role.setName("ROLE_ADMIN");
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(role));

        User savedUser = new User();
        savedUser.setId(10L);
        savedUser.setUsername("admin123");
        // ensure role set and admin profile present on returned savedUser
        savedUser.setRoles(new java.util.HashSet<>());
        savedUser.getRoles().add(role);
        ec.edu.espe.chat_real_time.model.user.AdminProfile savedProfile = new ec.edu.espe.chat_real_time.model.user.AdminProfile();
        savedProfile.setFirstName("Juan");
        savedProfile.setLastName("Lopez");
        savedProfile.setEmail("admin@mail.com");
        savedProfile.setPhone("0999999999");
        savedProfile.setUser(savedUser);
        savedUser.setAdminProfile(savedProfile);

        when(userService.saveUserDB(any(User.class))).thenReturn(Optional.of(savedUser));
        when(jwtService.generateAccessToken(savedUser)).thenReturn("token-abc");

        AuthResponse response = authService.registerAdmin(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("token-abc");

        verify(userService, times(1)).saveUserDB(any(User.class));
        verify(jwtService, times(1)).generateAccessToken(savedUser);
    }
}
