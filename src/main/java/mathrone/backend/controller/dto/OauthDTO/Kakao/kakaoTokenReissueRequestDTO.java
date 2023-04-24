package mathrone.backend.controller.dto.OauthDTO.Kakao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class kakaoTokenReissueRequestDTO {

    private String grantType;

    private String clientId;

    private String refreshToken;

    private String clientSecret;


}
