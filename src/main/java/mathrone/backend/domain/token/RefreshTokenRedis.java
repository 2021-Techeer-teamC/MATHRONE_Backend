package mathrone.backend.domain.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import mathrone.backend.config.CacheKey;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash(value = CacheKey.RefreshToken)
@AllArgsConstructor
@Builder
@Getter
public class RefreshTokenRedis {

    @Id
    private String id;
    private String refreshToken;
    // redis에서 설정한 시간 이후에 자동으로 해당 데이터가 사라지는 휘발성 데이터를 만들어주는 annotation
    @TimeToLive
    private Long expiration;
}

