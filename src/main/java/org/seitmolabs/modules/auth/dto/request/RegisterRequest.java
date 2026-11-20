package org.seitmolabs.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
@NotNull
public class RegisterRequest {
    
    @NotBlank 
    @Size(min=5,max=40)
    private String username;

    @NotBlank 
    @Size(min=5,max=40)
    private String password;

    @NotBlank 
    @Size(min=5,max=40)
    private String displayName;
}
