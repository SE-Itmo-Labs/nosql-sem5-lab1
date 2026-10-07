package org.seitmolabs.modules.user.domain;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.seitmolabs.modules.user.enums.Role;

@Getter 
@Setter 
@Builder 
@AllArgsConstructor
@NoArgsConstructor 
public class User {
    
    private String id;

    private String test;

    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
