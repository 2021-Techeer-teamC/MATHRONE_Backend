package mathrone.backend.domain.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import mathrone.backend.config.CacheKey;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@AllArgsConstructor
@Getter
@RedisHash(value = CacheKey.LogoutAccessToken)
@Builder
public class LogoutAccessToken {

    @Id
    private String id;

    private String accessId;

    @TimeToLive
    private Long expiration;

    public static LogoutAccessToken of(String accessToken, String accessId, Long remainExpirationMillSecond) {
        return LogoutAccessToken.builder()
                .id(accessToken)
                .accessId(accessId)
                .expiration(remainExpirationMillSecond)
                .build();
    }
}
