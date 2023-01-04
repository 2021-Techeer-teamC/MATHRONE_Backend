package mathrone.backend.controller.dto.OauthDTO.Kakao;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class KakaoIDToken {


    private String iss;
    private String aud;
    private String sub;
    private Integer iat;
    private Integer exp;
    private Integer auth_time;
//    private String nonce;
//    private String nickname;
    private String picture;
    private String email;

}
