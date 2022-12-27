package mathrone.backend.controller.dto.OauthDTO.Kakao;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class KakaoTokenResponseDTO {

    private String tokenType;

    private String accessToken;

    private String idToken;

    private Integer expiresIn;

    private String refreshToken;

    private Integer refreshTokenExpiresIn;

    private String scope;



}
