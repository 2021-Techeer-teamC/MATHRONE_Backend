package mathrone.backend.repository.redisRepository;

import mathrone.backend.domain.token.KakaoRefreshTokenRedis;
import org.springframework.data.repository.CrudRepository;

public interface KakaoRefreshTokenRedisRepository extends CrudRepository<KakaoRefreshTokenRedis, String> {

}
