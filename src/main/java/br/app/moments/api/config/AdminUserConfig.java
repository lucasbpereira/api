package br.app.moments.api.config;

import br.app.moments.api.user.Role;
import br.app.moments.api.user.RoleRepository;
import br.app.moments.api.user.User;
import br.app.moments.api.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.interfaces.RSAPrivateKey;
import java.util.Set;

@Configuration
public class AdminUserConfig implements CommandLineRunner {

    private RoleRepository roleRepository;
    private UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminUserConfig(RoleRepository roleRepository,
                           UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        Role roleAdmin = roleRepository.findByName(Role.Values.ADMIN.name());

        if (roleAdmin == null) {
            throw new IllegalStateException("A role ADMIN não foi encontrada no banco de dados.");
        }

        var userAdmin = userRepository.findByEmail("admin@moments.app.br");

        userAdmin.ifPresentOrElse(
                (user) -> {
                    System.out.println("Admin já existe");
                },
                () -> {
                    User user = new User();
                    user.setEmail("admin@moments.app.br");
                    user.setPassword(passwordEncoder.encode("123456"));
                    user.setFirstName("Admin");
                    user.setLastName("Moments");
                    user.setBirthDate("23/02/2025");
                    user.setPhone("123456789");
                    user.setRoles(Set.of(roleAdmin));

                    userRepository.save(user);
                }
        );
    }
}
