package ec.edu.espe.chat_real_time.Service.user;

import ec.edu.espe.chat_real_time.model.user.User;

public interface UserService {

  void  recordFailedLoginAttempt(String username);
  void recordSuccessfulLogin(String username);
  void saveUser(User user);
  void delete(User user);
}
