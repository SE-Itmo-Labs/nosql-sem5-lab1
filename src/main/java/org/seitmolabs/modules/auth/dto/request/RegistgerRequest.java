package org.seitmolabs.modules.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
public class RegistgerRequest {
    
    @NotBlank
    @Size(min=5,max=40)
    private String username;

    @Email 
    private String email;

    @NotBlank 
    @Size(min=5,max=40)
    private String password;
}
