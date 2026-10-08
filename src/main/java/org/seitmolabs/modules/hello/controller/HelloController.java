package org.seitmolabs.modules.hello.controller;

import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private final StringRedisTemplate redisTemplate;
    
    @GetMapping({"/api/v1/hello", "/hello"})
    public String hello() {

        String str_ = "msg:hello";

        String cachedValue = redisTemplate.opsForValue().get(str_);

        if (cachedValue != null) {  
            return cachedValue + " (из кэша Redis)";
        }

        String response = "Hello, World!";

        redisTemplate.opsForValue().set(str_, response, Duration.ofSeconds(60));

        return response + " (из сервера)";
    }

    public HelloController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}
