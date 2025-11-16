package ec.edu.espe.chat_real_time.Service;

import ec.edu.espe.chat_real_time.Service.user.UserServiceImpl;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setFailedLoginAttempts(0);
        user.setAccountNonLocked(true);
        user.setLockedUntil(null);
    }

    @Test
    void testRecordFailedLoginAttempt_success() {
        when(userRepository.findByUsername("testUser"))
                .thenReturn(Optional.of(user));

        userService.recordFailedLoginAttempt("testUser");

        assertEquals(1, user.getFailedLoginAttempts());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testRecordFailedLoginAttempt_triggersLock() {
        user.setFailedLoginAttempts(4);

        when(userRepository.findByUsername("testUser"))
                .thenReturn(Optional.of(user));

        userService.recordFailedLoginAttempt("testUser");

        // attempts should now be 5
        assertEquals(5, user.getFailedLoginAttempts());

        // lock should be applied
        assertFalse(user.isAccountNonLocked());
        assertNotNull(user.getLockedUntil());

        verify(userRepository, times(2)).save(user);
        // save 1: increment attempts
        // save 2: lockUserAccount()
    }

    @Test
    void testRecordFailedLoginAttempt_userNotFound() {
        when(userRepository.findByUsername("notfound"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userService.recordFailedLoginAttempt("notfound")
        );
    }

    @Test
    void testLockUserAccount_success() {
        when(userRepository.findByUsername("testUser"))
                .thenReturn(Optional.of(user));

        userService.lockUserAccount("testUser", 15);

        assertFalse(user.isAccountNonLocked());
        assertNotNull(user.getLockedUntil());
        verify(userRepository).save(user);
    }

    @Test
    void testLockUserAccount_userNotFound() {
        when(userRepository.findByUsername("missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userService.lockUserAccount("missing", 15)
        );
    }
  @Test
    void testRecordSuccessfulLogin_success() {
        user.setFailedLoginAttempts(3);
        user.setAccountNonLocked(false);
        user.setLockedUntil(LocalDateTime.now().plusMinutes(10));

        when(userRepository.findByUsername("testUser"))
                .thenReturn(Optional.of(user));

        userService.recordSuccessfulLogin("testUser");

        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
        assertTrue(user.isAccountNonLocked());
        verify(userRepository).save(user);
    }

    @Test
    void testRecordSuccessfulLogin_userNotFound() {
        when(userRepository.findByUsername("missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userService.recordSuccessfulLogin("missing")
        );
    }

    @Test
    void testSaveUser() {
        userService.saveUser(user);
        verify(userRepository).save(user);
    }

     @Test
    void testSaveUserDB() {
        when(userRepository.save(user)).thenReturn(user);
        Optional<User> result = userService.saveUserDB(user);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }
    @Test
    void testCleanExpiredGuests() {
        LocalDateTime now = LocalDateTime.now();

        userService.cleanExpiredGuests();

        verify(userRepository).deleteAllExpiredGuests(any(LocalDateTime.class));
    }

   @Test
    void testDeleteUser() {
        userService.delete(user);
        verify(userRepository).delete(user);
    }
}
