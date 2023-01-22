
package mathrone.backend.service;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import mathrone.backend.controller.dto.OauthDTO.GoogleIDToken;
import mathrone.backend.controller.dto.OauthDTO.Kakao.*;
import mathrone.backend.controller.dto.OauthDTO.OAuthLoginUtils;
import mathrone.backend.controller.dto.OauthDTO.RequestTokenDTO;
import mathrone.backend.controller.dto.OauthDTO.ResponseTokenDTO;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoIDToken;
import mathrone.backend.controller.dto.TokenDto;
import mathrone.backend.domain.token.KakaoRefreshTokenRedis;
import mathrone.backend.repository.tokenRepository.GoogleRefreshTokenRedisRepository;
import mathrone.backend.repository.tokenRepository.KakaoRefreshTokenRedisRepository;
import org.springframework.http.*;
import mathrone.backend.error.exception.ErrorCode;
import mathrone.backend.error.exception.UserException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SnsLoginService {
    //from Non-static method 'getClientId()' cannot be referenced from a static context error
    // 객체화 (static)되지 않았기 때문에 객체화 부터 시켜주어야 쓸수 있다.
    private final OAuthLoginUtils oAuthLoginUtils;
    private final KakaoOAuthLoginUtils kakaoOAuthLoginUtils;


    private  final GoogleRefreshTokenRedisRepository googleRefreshTokenRedisRepository;

    private final KakaoRefreshTokenRedisRepository kakaoRefreshTokenRedisRepository;

    SnsLoginService(OAuthLoginUtils oAuthLoginUtils, KakaoOAuthLoginUtils kakaoOAuthLoginUtils, GoogleRefreshTokenRedisRepository googleRefreshTokenRedisRepository,KakaoRefreshTokenRedisRepository kakaoRefreshTokenRedisRepository) {
        this.oAuthLoginUtils = oAuthLoginUtils;
        this.kakaoOAuthLoginUtils = kakaoOAuthLoginUtils;
        this.googleRefreshTokenRedisRepository = googleRefreshTokenRedisRepository;
        this.kakaoRefreshTokenRedisRepository = kakaoRefreshTokenRedisRepository;
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
    //kakao token얻기
    public ResponseEntity<KakaoTokenResponseDTO> getKakaoToken(String code) throws JsonProcessingException {
        try{
        RestTemplate rt = new RestTemplate();
        rt.setRequestFactory(new HttpComponentsClientHttpRequestFactory()); //error message type및 description확인 가능
        // 해더 만들기
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        // 바디 만들기 (HashMap 사용 불가!)
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoOAuthLoginUtils.getClientId());
        params.add("redirect_uri", kakaoOAuthLoginUtils.getKakaoRedirectUri());
        params.add("client_secret", kakaoOAuthLoginUtils.getClientSecret());
        params.add("code", code);
        // 해더와 바디를 하나의 오브젝트로 만들기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(params, headers);
        // Http 요청하고 리턴값을 response 변수로 받기
        ResponseEntity<String> apiResponseJson = rt.exchange(
                "https://kauth.kakao.com/oauth/token", // Host
                HttpMethod.POST, // Request Method
                kakaoTokenRequest,	// RequestBody
                String.class
        );	// return Object
            // ObjectMapper를 통해 String to Object로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // NULL이 아닌 값만 응답받기(NULL인 경우는 생략)
            KakaoTokenResponseDTO kakaoLoginResponse = objectMapper.readValue(apiResponseJson.getBody(), new TypeReference<KakaoTokenResponseDTO>() {
            });
            return ResponseEntity.ok().body(kakaoLoginResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body(null);
    }
    public ResponseEntity<KakaoIDToken> decodeIdToken(String idToken) throws JsonProcessingException {
        Map<String, Object> map = new HashMap<String, Object>();
        //1. ID토큰을 온점(.)을 기준으로 헤더,페이로드,서명을 분리
        String[] params = idToken.split("\\."); //escape 필수
        //2. 페이로드를 Base64방식으로 디코드
        Base64.Decoder decoder = Base64.getDecoder();
        String payload = new String(decoder.decode(params[1])); //0 : header / 1 : payload / 2 : signature
        /*
        {
        "aud":"adfadfadsf","sub":"2598284331","auth_time":1672239086,
        "iss":"https://kauth.kakao.com","exp":1672260686,"iat":1672239086,
        "picture":"qwefqwe.jpg","email":"qwefqwefqfew@naver.com"
        }
         */
        ObjectMapper mapper = new ObjectMapper();
        KakaoIDToken returnMap = mapper.readValue(payload, KakaoIDToken.class);
        return ResponseEntity.ok().body(returnMap);
    }



    @Transactional
    public ResponseEntity<KakaoTokenResponseDTO> kakaoReissue(String userId) {

        //리프레시토큰으로 재발급부터
        Optional<KakaoRefreshTokenRedis> kakaoRedis = kakaoRefreshTokenRedisRepository.findById(userId);

        String refreshToken = kakaoRedis.get().getRefreshToken();


        try{
            RestTemplate rt = new RestTemplate();
            rt.setRequestFactory(new HttpComponentsClientHttpRequestFactory()); //error message type및 description확인 가능
            // 해더 만들기
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8"); // http converter 존재x에러 해결(multimap)
            // 바디 만들기 (HashMap 사용 불가!)
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "refresh_token");
            params.add("client_id", kakaoOAuthLoginUtils.getClientId());
            params.add("client_secret", kakaoOAuthLoginUtils.getClientSecret());
            params.add("refresh_token", refreshToken);
            // 해더와 바디를 하나의 오브젝트로 만들기
            HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                    new HttpEntity<>(params, headers);
            // Http 요청하고 리턴값을 response 변수로 받기
            ResponseEntity<String> apiResponseJson = rt.exchange(
                    "https://kauth.kakao.com/oauth/token", // Host
                    HttpMethod.POST, // Request Method
                    kakaoTokenRequest,	// RequestBody
                    String.class
            );	// return Object
            // ObjectMapper를 통해 String to Object로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // NULL이 아닌 값만 응답받기(NULL인 경우는 생략)
            KakaoTokenResponseDTO kakaoLoginResponse = objectMapper.readValue(apiResponseJson.getBody(), new TypeReference<KakaoTokenResponseDTO>() {
            });
            return ResponseEntity.ok().body(kakaoLoginResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body(null);

    }

}
