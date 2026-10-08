package org.seitmolabs.modules.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.seitmolabs.modules.user.domain.User;
import org.seitmolabs.modules.user.enums.Role;

@Getter 
@Setter 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
public class AuthResponse {
    private Long userId;
    private String username;
    private String displayName;
    private Role role;

    public static AuthResponse from(User user) {
        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .role(user.getRole())
                .build();
    }
}
