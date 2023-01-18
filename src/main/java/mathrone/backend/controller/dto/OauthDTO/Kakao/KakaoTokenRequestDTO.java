package mathrone.backend.controller.dto.OauthDTO.Kakao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component; //Component 사용 시 에러



//@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class KakaoTokenRequestDTO {

    private String grant_type;

    private String client_id;

    private String redirect_uri;

    private String code;

    private String client_secret;

}
