package ec.edu.espe.chat_real_time.repository;

import ec.edu.espe.chat_real_time.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(String name);
  boolean existsByName(String name);
}