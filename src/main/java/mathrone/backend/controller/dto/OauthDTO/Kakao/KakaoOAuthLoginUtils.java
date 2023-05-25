package mathrone.backend.controller.dto.OauthDTO.Kakao;

import lombok.*;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;


@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class KakaoOAuthLoginUtils {

    @Value("${kakao.auth.url}")
    private String kakaoAuthUrl;

    @Value("${kakao.token.url}")
    private String kakaoTokenUrl;

    @Value("${kakao.redirect.uri}")
    private String kakaoRedirectUri;

    @Value("${kakao.client.id}")
    private String clientId;

    @Value("${kakao.client.secret}")
    private String clientSecret;

    @Value("${kakao.auth.grantType}")
    private String grantType;

    @Value("${kakao.admin.key}")
    private String adminKey;
}
