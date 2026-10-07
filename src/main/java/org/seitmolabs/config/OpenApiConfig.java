package org.seitmolabs.config;

import java.util.LinkedList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration 
public class OpenApiConfig {
    
    @Bean 
    public OpenAPI openApiConfig() {
        var info = new Info();

        info.setTitle("NoSQL API");
        info.setDescription("Описание REST API первой лр по носкл");
        info.setVersion("0.0.1");

        var servers = new LinkedList<Server>();
        servers.add(new Server().url("http://localhost:8080").description("Local"));

        return new OpenAPI().info(info);
    }
}
