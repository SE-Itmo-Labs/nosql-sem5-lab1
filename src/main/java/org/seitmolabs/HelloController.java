package org.seitmolabs;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private final StringRedisTemplate redisTemplate;
    
    @GetMapping("/hello")
    public String hello() {

        String str_ = "msg:hello";

        String cachedValue = redisTemplate.opsForValue().get(str_);

        if (cachedValue != null) {
            return cachedValue + " (из кэша Redis)";
        }

        String response = "Hello, World!";

        redisTemplate.opsForValue().set(str_, response, 60, TimeUnit.SECONDS);

        return response + " (из сервера)";
    }

    public HelloController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}
