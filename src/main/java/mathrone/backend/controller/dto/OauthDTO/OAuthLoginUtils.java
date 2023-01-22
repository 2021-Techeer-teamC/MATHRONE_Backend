package mathrone.backend.controller.dto.OauthDTO;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value; //이 Value annotation을 사용해야한다.. 다른거 썼다가 null뜸

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@Component
@Getter
public class OAuthLoginUtils {

    @Value("${google.auth.url}")
    private String googleAuthUrl;

    @Value("${google.login.url}")
    private String googleLoginUrl;

    @Value("${google.client.id}")
    private String clientId; // Google(Resource Server)에서 Client(나의 서비스)를 구분하는 id값

    @Value("${google.redirect.uri}")
    private String redirectUri; // 요청승인한 경우 반환되는 주소

    @Value("${google.secret}")
    private String clientSecret; // pw

    @Value("${google.auth.scope}")
    private String scope;   // OAuth 동의하는 기능의 범위


    private String code; // accesstoken을 발급 받기 전 임시 비밀번호 (authorization Code)

    private String grantType; // 어떤 방식으로 Google/사용자/나의 서비스 가 소통을 할것인지 방식을 선택한다(4가지 방식이 있는데 대표적으로 auth code방식이 있음 )


    public String getGoogleAuthUrl() {
        return googleAuthUrl;
    }

    public String getGoogleLoginUrl() {
        return googleLoginUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getScope() {
        return scope;
    }

    public String getCode() {
        return code;
    }

    public String getGrantType() {
        return grantType;
    }

    // scope의 값을 보내기 위해 띄어쓰기 값을 UTF-8로 변환하는 로직 포함
    public String getScopeUrl() {
//        return scopes.stream().collect(Collectors.joining(","))
//                .replaceAll(",", "%20");
        return scope.replaceAll(",", "%20");
    }
}
