package org.seitmolabs.modules.user.repository;

import java.util.Optional;

import org.seitmolabs.modules.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository 
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
