package ec.edu.espe.chat_real_time.Service.user;

import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements  UserService{

  private final UserRepository userRepository;



  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void recordFailedLoginAttempt(String username) {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    int attempts = user.getFailedLoginAttempts() + 1;
    user.setFailedLoginAttempts(attempts);

    if (attempts >= 5) {
      lockUserAccount(username, 15); // Lock account for 15 minutes after 5 failed attempts
    }

    userRepository.save(user);
  }

  @Transactional
  public void lockUserAccount(String username, int durationMinutes) {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    user.setLockedUntil(LocalDateTime.now().plusMinutes(durationMinutes));
    user.setAccountNonLocked(false);
    userRepository.save(user);
  }

  @Transactional
  public void saveUser(User user){
    userRepository.save(user);
  }

  @Transactional
  public Optional<User> saveUserDB(User user) {
    return Optional.of(userRepository.save(user));
  }

  @Scheduled(fixedRate = 3600000)
  @Transactional
  public void cleanExpiredGuests() {
    userRepository.deleteAllExpiredGuests(LocalDateTime.now());
  }

  @Override
  public User findByUsername(String username) {
    return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
  }

  @Transactional
  @Override
  public void recordSuccessfulLogin(String username) {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    user.setFailedLoginAttempts(0);
    user.setLockedUntil(null);
    user.setAccountNonLocked(true);

    userRepository.save(user);
  }

  @Transactional
  @Override
  public void delete(User user) {
    userRepository.delete(user);
  }



}
