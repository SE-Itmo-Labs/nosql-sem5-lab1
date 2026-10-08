package org.seitmolabs.config;

import org.seitmolabs.modules.user.domain.User;
import org.seitmolabs.modules.user.enums.Role;
import org.seitmolabs.modules.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner createTestUsers(UserRepository userRepository) {
        return args -> {
            createUserIfMissing(userRepository, "client01", "client123", "Тестовый клиент", Role.ROLE_USER);
            createUserIfMissing(userRepository, "admin", "123", "Администратор", Role.ROLE_ADMIN);
        };
    }

    private void createUserIfMissing(
            UserRepository userRepository,
            String username,
            String password,
            String displayName,
            Role role
    ) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }

        userRepository.save(User.builder()
                .username(username)
                .password(password)
                .displayName(displayName)
                .role(role)
                .build());
    }
}
