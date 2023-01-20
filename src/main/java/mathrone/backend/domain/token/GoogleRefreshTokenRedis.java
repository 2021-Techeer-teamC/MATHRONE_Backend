package mathrone.backend.domain.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
@RedisHash("GoogleRefreshToken")
@AllArgsConstructor
@Builder
@Getter
public class GoogleRefreshTokenRedis {

    @Id
    private String id; //회원의 primay key id
    private String refreshToken; // google에서 발급해준 refresh token

    /*
    참고 : 구글은 refreshtoken의 유효시간이 무한이다. (엑세스를 취소할 떄 까지 유효함)
     */

}
