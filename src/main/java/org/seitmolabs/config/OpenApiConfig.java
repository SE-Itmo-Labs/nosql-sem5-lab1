package org.seitmolabs.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration 
public class OpenApiConfig {
    
    @Bean 
    public OpenAPI openApi() {
        var info = new Info();

        info.setTitle("NoSQL API");
        info.setDescription("Описание REST API первой лр по носкл");
        info.setVersion("0.0.1");

        return new OpenAPI().info(info);
    }
}
