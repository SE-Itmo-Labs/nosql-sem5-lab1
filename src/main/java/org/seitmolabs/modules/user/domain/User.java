package org.seitmolabs.modules.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.seitmolabs.modules.user.enums.Role;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Setter 
@Getter
@Builder 
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private Long id;

    private String username;

    private String password;

    private String displayName;

    @Builder.Default
    private Role role = Role.ROLE_USER;
}
