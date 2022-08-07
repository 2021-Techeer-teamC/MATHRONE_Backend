package mathrone.backend.domain.token;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.TimeToLive;

import javax.persistence.*;
import java.util.Date;

@Getter
@NoArgsConstructor
@Table(name = "refreshtoken")
@Entity
public class RefreshToken {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "expiration")
    private Date expiration;

    @Builder
    public RefreshToken(String userid,String refreshToken, Date expiration) {
        this.userId = userid;
        this.refreshToken = refreshToken;
        this.expiration = expiration;
    }

    public RefreshToken updateValue(String refreshToken, Date expiration) {
        this.refreshToken = refreshToken;
        this.expiration = expiration;
        return this;
    }

    public RefreshTokenRedis transferRedisToken() {
        return RefreshTokenRedis.builder()
                .id(this.userId)
                .refreshToken(this.refreshToken)
                // Redis의 TimeToLive annotation 단위는 int, 즉 초로 계산함
                .expiration((this.expiration.getTime()-(new Date().getTime()))/1000)
                .build();
    }

}
