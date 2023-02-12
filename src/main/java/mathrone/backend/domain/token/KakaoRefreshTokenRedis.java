package mathrone.backend.domain.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash("KakaoRefreshToken")
@AllArgsConstructor
@Builder
@Getter
public class KakaoRefreshTokenRedis {

    @Id
    private String id; //회원의 primay key id
    private String refreshToken; // kakao에서 발급해준 refresh token
    // redis에서 설정한 시간 이후에 자동으로 해당 데이터가 사라지는 휘발성 데이터를 만들어주는 annotation
    @TimeToLive
    private Integer expiration;//만료시간


}
