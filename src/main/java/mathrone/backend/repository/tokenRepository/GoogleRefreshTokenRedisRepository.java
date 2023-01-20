package mathrone.backend.repository.tokenRepository;

import mathrone.backend.domain.token.GoogleRefreshTokenRedis;
import org.springframework.data.repository.CrudRepository;

public interface GoogleRefreshTokenRedisRepository extends CrudRepository<GoogleRefreshTokenRedis, String> {
}
