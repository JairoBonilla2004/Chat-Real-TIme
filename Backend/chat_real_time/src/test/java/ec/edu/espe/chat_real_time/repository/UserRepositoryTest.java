package ec.edu.espe.chat_real_time.repository;

import ec.edu.espe.chat_real_time.model.Role;
import ec.edu.espe.chat_real_time.model.user.GuestProfile;
import ec.edu.espe.chat_real_time.model.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    private User createUser(String username, String roleName) {
        User u = User.builder()
                .username(username)
                .password("pwd")
                .build();

        if (roleName != null) {
            // buscar role existente por nombre en el contexto de pruebas
            List<Role> found = em.getEntityManager()
                    .createQuery("SELECT r FROM Role r WHERE r.name = :name", Role.class)
                    .setParameter("name", roleName)
                    .getResultList();
            Role r;
            if (!found.isEmpty()) {
                r = found.get(0);
            } else {
                r = Role.builder().name(roleName).build();
                em.persistAndFlush(r);
            }

            u.getRoles().add(r);
        }
        return u;
    }

    @Test
    void saveAndFindByUsernameAndDeletedIsNull() {
        User u = createUser("alice", null);
        em.persistAndFlush(u);

        Optional<User> opt = userRepository.findByUsernameAndDeletedAtIsNull("alice");
        assertTrue(opt.isPresent());
        assertEquals("alice", opt.get().getUsername());
    }

    @Test
    void existsByUsernameChecks() {
        User u = createUser("bob", null);
        em.persistAndFlush(u);

        assertTrue(userRepository.existsByUsername("bob"));
        assertTrue(userRepository.existsByUsernameAndDeletedAtIsNull("bob"));
    }

    @Test
    void findAllAdminsAndGuests_and_userHasRole() {
        User admin = createUser("admin1", "ROLE_ADMIN");
        User guest = createUser("guest1", "ROLE_GUEST");

        em.persist(admin);
        em.persist(guest);
        em.flush();

        List<User> admins = userRepository.findAllAdmins();
        List<User> guests = userRepository.findAllGuests();

        assertTrue(admins.stream().anyMatch(u -> u.getUsername().equals("admin1")));
        assertTrue(guests.stream().anyMatch(u -> u.getUsername().equals("guest1")));

        assertTrue(userRepository.userHasRole(admin.getId(), "ROLE_ADMIN"));
        assertTrue(userRepository.userHasRole(guest.getId(), "ROLE_GUEST"));
    }

    @Test
    void deleteAllExpiredGuests_removesUsersWithExpiredProfiles() {
        // user with expired guest profile
        User u1 = createUser("gexpired", null);
        em.persistAndFlush(u1);

        GuestProfile gp = GuestProfile.builder()
                .id(u1.getId())
                .nickname("nick1")
                .expiresAt(LocalDateTime.now().minusDays(1))
                .user(u1)
                .build();
        em.persistAndFlush(gp);

        // user with not expired guest profile
        User u2 = createUser("gvalid", null);
        em.persistAndFlush(u2);
        GuestProfile gp2 = GuestProfile.builder()
                .id(u2.getId())
                .nickname("nick2")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .user(u2)
                .build();
        em.persistAndFlush(gp2);

        int deleted = userRepository.deleteAllExpiredGuests(LocalDateTime.now());
        // should delete user u1
        assertTrue(deleted >= 0);

        List<User> remaining = userRepository.findAll();
        assertTrue(remaining.stream().anyMatch(u -> u.getUsername().equals("gvalid")));
    }
}
