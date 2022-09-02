package mathrone.backend.controller.dto.OauthDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class RequestTokenDTO {

    private String clientId; // Google(Resource Server)에서 Client(나의 서비스)를 구분하는 id값

    private String redirectUri; // 요청승인한 경우 반환되는 주소

    private String clientSecret; // pw

    private String scope;   // OAuth 동의하는 기능의 범위


    private String code; // accesstoken을 발급 받기 전 임시 비밀번호 (authorization Code)

    private String grantType; // 어떤 방식으로 Google/사용자/나의 서비스 가 소통을 할것인지 방식을 선택한다(4가지 방식이 있는데 대표적으로 auth code방식이 있음 )


}
