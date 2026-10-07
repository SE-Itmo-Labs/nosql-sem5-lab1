package org.seitmolabs.security.userdetails;

import org.seitmolabs.modules.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

// вызывается при аунтентификации
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null; // временно
    }
    
}
