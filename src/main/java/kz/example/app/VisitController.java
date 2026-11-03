package kz.example.app;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VisitController {
    private final StringRedisTemplate redis;
    private final String appVersion;

    public VisitController(StringRedisTemplate redis,
                           @Value("${APP_VERSION:stable}") String appVersion) {
        this.redis = redis;
        this.appVersion = appVersion;
    }

    @GetMapping("/")
    public Map<String, Object> home() {
        Long visits = redis.opsForValue().increment("visits");
        return Map.of(
            "message", "Java + Redis is running!",
            "visits", visits == null ? 0 : visits,
            "version", appVersion
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
