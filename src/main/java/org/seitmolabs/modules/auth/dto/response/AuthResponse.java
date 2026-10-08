package org.seitmolabs.modules.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
public class AuthResponse {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private String username;
    private String email;
}
