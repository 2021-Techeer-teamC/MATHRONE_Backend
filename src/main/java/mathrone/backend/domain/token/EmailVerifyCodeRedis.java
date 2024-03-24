package mathrone.backend.domain.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import javax.persistence.Id;

@AllArgsConstructor
@Builder
@Getter
@RedisHash("EmailVerifyCode")
public class EmailVerifyCodeRedis {

    @Id
    private String id; //accountId를 의미 id를 사용하지 않으면 에러

    private String verifyCode;

    // redis에서 설정한 시간 이후에 자동으로 해당 데이터가 사라지는 휘발성 데이터를 만들어주는 annotation
    @TimeToLive
    private Long expiration;//3분 유효

}
