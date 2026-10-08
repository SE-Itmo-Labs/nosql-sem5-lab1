package org.seitmolabs.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration 
public class OpenApiConfig {
    
    @Bean 
    public OpenAPI openApi() {
        var info = new Info();

        info.setTitle("NoSQL API");
        info.setDescription("Описание REST API первой лр по носкл");
        info.setVersion("0.0.1");

        Components components = new Components()
                .addSecuritySchemes("username", headerScheme("X-Username"))
                .addSecuritySchemes("password", headerScheme("X-Password"));

        SecurityRequirement credentials = new SecurityRequirement()
                .addList("username")
                .addList("password");

        return new OpenAPI()
                .info(info)
                .components(components)
                .addSecurityItem(credentials);
    }

    private SecurityScheme headerScheme(String headerName) {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(headerName);
    }
}
