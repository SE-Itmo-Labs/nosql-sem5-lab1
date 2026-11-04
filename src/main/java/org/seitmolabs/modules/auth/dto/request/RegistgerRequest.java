package org.seitmolabs.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
@NotNull
@NotBlank  RegisterRequest
public class RegistgerRequest {
    
    @Size(min=5,max=40)
    private String username;

    @Size(min=5,max=40)
    private String password;

    @Size(min=5,max=40)
    private String displayName;
}
