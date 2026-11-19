package org.seitmolabs.demo;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.modules.user.domain.User;
import org.seitmolabs.modules.user.enums.Role;
import org.seitmolabs.modules.user.repository.UserRepository;
import org.springframework.core.annotation.Order;

@Order(10)
@RequiredArgsConstructor
class UsersDemo implements DemoScenario {
    private final UserRepository userRepository;

    @Override
    public void run() {
        createUserIfMissing("user", "12345", "Тестовый клиент", Role.ROLE_USER);
        createUserIfMissing("admin", "12345", "Администратор", Role.ROLE_ADMIN);
        createUserIfMissing("demo-guest", "12345", "Тестовый зритель", Role.ROLE_USER);
    }

    private void createUserIfMissing(
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
