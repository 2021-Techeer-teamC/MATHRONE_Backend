package mathrone.backend.repository.redisRepository;

import mathrone.backend.domain.token.ReactivateCodeRedis;
import org.springframework.data.repository.CrudRepository;

public interface ReactivateCodeRedisRepository extends CrudRepository<ReactivateCodeRedis, String> {
}
