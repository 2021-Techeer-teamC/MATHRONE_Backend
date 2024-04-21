package mathrone.backend.repository.redisRepository;

import mathrone.backend.domain.token.EmailVerifyCodeRedis;
import org.springframework.data.repository.CrudRepository;

public interface EmailVerifyCodeRedisRepository  extends CrudRepository<EmailVerifyCodeRedis, String> {
}
