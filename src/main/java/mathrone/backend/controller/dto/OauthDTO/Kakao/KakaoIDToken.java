package mathrone.backend.controller.dto.OauthDTO.Kakao;

import org.springframework.stereotype.Component;

@Component
public class KakaoIDToken {


    private String iss;
    private String aud;
    private String sub;
    private Integer iat;
    private Integer exp;
    private Integer auth_time;
    private String nonce;
    private String nickname;
    private String picture;
    private String email;

}
