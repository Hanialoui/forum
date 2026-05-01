package tn.esprit.forum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.cache.type=none",
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379",
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false"
})
class ForumApplicationTests {
    @Test
    void contextLoads() {
    }
}
