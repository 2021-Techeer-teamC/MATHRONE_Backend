package mathrone.backend.controller.dto.OauthDTO.Kakao;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class KakaoTokenResponseDTO {

    private String token_type;

    private String access_token;

    private String id_token;

    private Integer expires_in;

    private String refresh_token;

    private Integer refresh_token_expires_in;

    private String scope;


}
