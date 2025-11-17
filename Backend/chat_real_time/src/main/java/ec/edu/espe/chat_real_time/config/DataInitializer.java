package ec.edu.espe.chat_real_time.config;


import ec.edu.espe.chat_real_time.model.Role;
import ec.edu.espe.chat_real_time.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

  @Bean
  CommandLineRunner init(RoleRepository roleRepository) {
    return args -> {

      if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
        roleRepository.save(Role.builder()
                .name("ROLE_ADMIN")
                .description("Administrador del sistema")
                .build());
      }

      if (roleRepository.findByName("ROLE_GUEST").isEmpty()) {
        roleRepository.save(Role.builder()
                .name("ROLE_GUEST")
                .description("Usuario invitado")
                .build());
      }

    };
  }
}
