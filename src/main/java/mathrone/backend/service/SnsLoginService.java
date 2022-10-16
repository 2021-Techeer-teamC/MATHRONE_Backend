
package mathrone.backend.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.nimbusds.jose.shaded.json.JSONObject;
import mathrone.backend.controller.dto.OauthDTO.GoogleIDToken;
import mathrone.backend.controller.dto.OauthDTO.OAuthLoginUtils;
import mathrone.backend.controller.dto.OauthDTO.RequestTokenDTO;
import mathrone.backend.controller.dto.OauthDTO.ResponseTokenDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@Service
public class SnsLoginService {

    //from Non-static method 'getClientId()' cannot be referenced from a static context error
    // 객체화 (static)되지 않았기 때문에 객체화 부터 시켜주어야 쓸수 있다.
    private final OAuthLoginUtils oAuthLoginUtils;

    SnsLoginService(OAuthLoginUtils oAuthLoginUtils) {
        this.oAuthLoginUtils = oAuthLoginUtils;
    }


    public ResponseEntity<ResponseTokenDTO> getToken(String code) {

        System.out.println("1");
        System.out.println(code);

        //이렇게 바로 가져오는 방식으로 하고 builder를 사용하면
        //https://wildeveloperetrain.tistory.com/143 이런 이유로 NULL이된다 ... (value들이)
//        RestTemplate restTemplate = new RestTemplate();
//        RequestTokenDTO requestParams = RequestTokenDTO.builder()
//                    .code(code)
//                    .grantType("authorization_code")
//                    .build();
//        System.out.println("2");
//
//        System.out.println("clientID : "+requestParams.getClientId());
//        System.out.println("clientSecret : "+requestParams.getClientSecret());
//        System.out.println("redirectURL : "+requestParams.getRedirectUri());
//        System.out.println("Grantype : "+requestParams.getGrantType());
//        System.out.println("code : "+requestParams.getCode());
//        System.out.println("scope : "+requestParams.getScope());


        RestTemplate restTemplate = new RestTemplate();
        RequestTokenDTO requestParams = RequestTokenDTO.builder()
                .clientId(oAuthLoginUtils.getClientId())
                .clientSecret(oAuthLoginUtils.getClientSecret())
                .code(code)
                .redirectUri(oAuthLoginUtils.getRedirectUri())
                .grantType("authorization_code")
                .accessType("offline")//refresh token을 제공함
                .build();


        try {
            // Http Header 설정

            System.out.println("3");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            System.out.println("4");
            HttpEntity<RequestTokenDTO> httpRequestEntity = new HttpEntity<>(requestParams, headers);
            System.out.println("5");
            ResponseEntity<String> apiResponseJson = restTemplate.postForEntity("https://accounts.google.com/o/oauth2/token", httpRequestEntity, String.class);
            System.out.println("6");


            // ObjectMapper를 통해 String to Object로 변환
            ObjectMapper objectMapper = new ObjectMapper();

            System.out.println("7");
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            System.out.println("8");
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // NULL이 아닌 값만 응답받기(NULL인 경우는 생략)
            System.out.println("9");
            ResponseTokenDTO googleLoginResponse = objectMapper.readValue(apiResponseJson.getBody(), new TypeReference<ResponseTokenDTO>() {
            });

            System.out.println(apiResponseJson.getBody());
            System.out.println("10");
            System.out.println(googleLoginResponse.getAccessToken());
            System.out.println("11");
            System.out.println(googleLoginResponse.getRefreshToken());
            System.out.println("12");
            System.out.println(googleLoginResponse.getExpiresIn());
            System.out.println("13");
            System.out.println(googleLoginResponse.getTokenType());
            System.out.println("14");
            System.out.println(googleLoginResponse.getIdToken());
            System.out.println("15");
            return ResponseEntity.ok().body(googleLoginResponse);

        } catch (Exception e) {
            System.out.println("error in backend");
            e.printStackTrace();
        }

        return ResponseEntity.badRequest().body(null);

    }


    public ResponseEntity<GoogleIDToken> getGoogleIDToken(ResponseEntity<ResponseTokenDTO> googleTokenInfo) throws Exception {

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        // 사용자의 정보는 JWT Token으로 저장되어 있고, Id_Token에 값을 저장한다.
        String jwtToken = googleTokenInfo.getBody().getIdToken();

        // JWT Token을 전달해 JWT 저장된 사용자 정보 확인
        String requestUrl = UriComponentsBuilder.fromHttpUrl(oAuthLoginUtils.getGoogleAuthUrl() + "/tokeninfo").queryParam("id_token", jwtToken).toUriString();

        String resultJson = restTemplate.getForObject(requestUrl, String.class);

        if (resultJson != null) {
            GoogleIDToken userInfoDto = objectMapper.readValue(resultJson, new TypeReference<GoogleIDToken>() {
            });

            System.out.println("google id token ㅂㅏㄷ아내기");
            System.out.println(resultJson);

            System.out.println("리턴되는 값 ");
            System.out.println(userInfoDto);

            return ResponseEntity.ok().body(userInfoDto);
        } else {
            throw new Exception("Google OAuth failed!");
        }

    }

}
