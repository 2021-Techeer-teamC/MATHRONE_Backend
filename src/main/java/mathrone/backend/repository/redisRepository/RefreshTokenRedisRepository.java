package mathrone.backend.repository.redisRepository;

import mathrone.backend.domain.token.RefreshTokenRedis;
import org.springframework.data.repository.CrudRepository;



public interface RefreshTokenRedisRepository extends CrudRepository<RefreshTokenRedis, String> {
}
