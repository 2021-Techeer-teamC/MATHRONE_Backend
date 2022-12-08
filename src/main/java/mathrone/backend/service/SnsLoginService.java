
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
import mathrone.backend.error.exception.ErrorCode;
import mathrone.backend.error.exception.UserException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static mathrone.backend.domain.enums.UserResType.GOOGLE;


@Service
public class SnsLoginService {

    //from Non-static method 'getClientId()' cannot be referenced from a static context error
    // 객체화 (static)되지 않았기 때문에 객체화 부터 시켜주어야 쓸수 있다.
    private final OAuthLoginUtils oAuthLoginUtils;

    SnsLoginService(OAuthLoginUtils oAuthLoginUtils) {
        this.oAuthLoginUtils = oAuthLoginUtils;
    }


    public ResponseEntity<ResponseTokenDTO> getToken(String code) {

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


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<RequestTokenDTO> httpRequestEntity = new HttpEntity<>(requestParams, headers);

            ResponseEntity<String> apiResponseJson = restTemplate.postForEntity("https://accounts.google.com/o/oauth2/token", httpRequestEntity, String.class);


            // ObjectMapper를 통해 String to Object로 변환
            ObjectMapper objectMapper = new ObjectMapper();

            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // NULL이 아닌 값만 응답받기(NULL인 경우는 생략)

            ResponseTokenDTO googleLoginResponse = objectMapper.readValue(apiResponseJson.getBody(), new TypeReference<ResponseTokenDTO>() {
            });

            return ResponseEntity.ok().body(googleLoginResponse);

        } catch (Exception e) {
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


        resultNull(resultJson); //resultjson이 null이면 여기서 에러 발생

        //null이 아니면 정상 작동
        GoogleIDToken userInfoDto = objectMapper.readValue(resultJson, new TypeReference<GoogleIDToken>() {});

        return ResponseEntity.ok().body(userInfoDto);


    }


    public void resultNull(String resultJson){
        if(resultJson==null){
            throw new UserException(ErrorCode.GOOGLE_SERVER_ERROR);
        }
    }


}
