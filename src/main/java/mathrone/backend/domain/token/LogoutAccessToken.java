package mathrone.backend.domain.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@AllArgsConstructor
@Getter
@RedisHash(value = "logoutAccessToken")
@Builder
public class LogoutAccessToken {

    @Id
    private String id;

    private String email;

    @TimeToLive
    private Long expiration;

    public static LogoutAccessToken of(String accessToken, String email, Long remainExpirationMillSecond) {
        return LogoutAccessToken.builder()
                .id(accessToken)
                .email(email)
                .expiration(remainExpirationMillSecond)
                .build();
    }
}
