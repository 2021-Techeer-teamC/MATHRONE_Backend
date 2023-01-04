package mathrone.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoTokenResponseDTO;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDto {
    private String grantType;
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresIn;
    private UserResponseDto userInfo;
    private KakaoTokenResponseDTO snsInfo;
}
