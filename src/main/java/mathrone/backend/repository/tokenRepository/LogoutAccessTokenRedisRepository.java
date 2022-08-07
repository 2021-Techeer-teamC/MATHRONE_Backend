package mathrone.backend.repository.tokenRepository;

import mathrone.backend.domain.token.LogoutAccessToken;
import org.springframework.data.repository.CrudRepository;

public interface LogoutAccessTokenRedisRepository extends CrudRepository<LogoutAccessToken, String> {
}
