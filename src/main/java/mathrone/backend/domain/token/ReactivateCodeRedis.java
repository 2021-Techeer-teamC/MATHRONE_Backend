package mathrone.backend.domain.token;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@AllArgsConstructor
@Builder
@Getter
@RedisHash("ReactivateCode")
public class ReactivateCodeRedis {

    @Id
    private String id; //accountId를 의미 id를 사용하지 않으면 에러

    private String activateCode;

    // redis에서 설정한 시간 이후에 자동으로 해당 데이터가 사라지는 휘발성 데이터를 만들어주는 annotation
    @TimeToLive
    private Long expiration;//3분 유효

}
