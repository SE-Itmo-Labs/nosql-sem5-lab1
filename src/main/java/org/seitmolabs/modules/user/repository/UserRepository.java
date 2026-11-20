package org.seitmolabs.modules.user.repository;

import java.util.Optional;

import org.seitmolabs.modules.user.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
