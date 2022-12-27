
package mathrone.backend.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import mathrone.backend.controller.dto.OauthDTO.GoogleIDToken;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoOAuthLoginUtils;
import mathrone.backend.controller.dto.OauthDTO.OAuthLoginUtils;
import mathrone.backend.controller.dto.OauthDTO.RequestTokenDTO;
import mathrone.backend.controller.dto.OauthDTO.ResponseTokenDTO;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoTokenRequestDTO;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoTokenResponseDTO;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


@Service
public class SnsLoginService {

    //from Non-static method 'getClientId()' cannot be referenced from a static context error
    // 객체화 (static)되지 않았기 때문에 객체화 부터 시켜주어야 쓸수 있다.
    private final OAuthLoginUtils oAuthLoginUtils;
    private final KakaoOAuthLoginUtils kakaoOAuthLoginUtils;

    SnsLoginService(OAuthLoginUtils oAuthLoginUtils, KakaoOAuthLoginUtils kakaoOAuthLoginUtils) {
        this.oAuthLoginUtils = oAuthLoginUtils;
        this.kakaoOAuthLoginUtils = kakaoOAuthLoginUtils;
    }


    //google
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

        if (resultJson != null) {
            GoogleIDToken userInfoDto = objectMapper.readValue(resultJson, new TypeReference<GoogleIDToken>() {
            });

            return ResponseEntity.ok().body(userInfoDto);
        } else {
            throw new Exception("Google OAuth failed!");
        }

    }


    //kakao token얻기
    public ResponseEntity<KakaoTokenResponseDTO> getKakaoToken(String code) throws JsonProcessingException {

        try{

            System.out.println("1");
        RestTemplate rt = new RestTemplate();

        // 해더 만들기
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            System.out.println("2");

        // 바디 만들기 (HashMap 사용 불가!)
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoOAuthLoginUtils.getClientId());
        params.add("redirect_uri", kakaoOAuthLoginUtils.getKakaoRedirectUri());
        params.add("client_secret", kakaoOAuthLoginUtils.getClientSecret());
        params.add("code", code);

            System.out.println("3");

        // 해더와 바디를 하나의 오브젝트로 만들기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(params, headers);

        // Http 요청하고 리턴값을 response 변수로 받기
        ResponseEntity<String> apiResponseJson = rt.exchange(
                "https://kauth.kakao.com/oauth/token", // Host
                HttpMethod.POST, // Request Method
                kakaoTokenRequest,	// RequestBody
                String.class);	// return Object

            System.out.println("4");

        System.out.println(apiResponseJson);

//        RestTemplate restTemplate = new RestTemplate();
//        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
//
//        System.out.println("1");
//
//        KakaoTokenRequestDTO requestParams = KakaoTokenRequestDTO.builder()
//                .grant_type(kakaoOAuthLoginUtils.getGrantType())
//                .client_id(kakaoOAuthLoginUtils.getClientId())
//                .redirect_uri(kakaoOAuthLoginUtils.getKakaoRedirectUri())
//                .client_secret(kakaoOAuthLoginUtils.getClientSecret())
//                .code(code)
//                .build();
//
//
////        System.out.println(requestParams.getCode());
////        System.out.println(requestParams.getGrantType());
////        System.out.println(requestParams.getClientId());
////        System.out.println(requestParams.getRedirectURI());
//
//        System.out.println("2");
//
//        try {
//            // Http Header 설정
//
//            System.out.println("3");
//
//            /*
//            error 401 unauthorized
//            https://devtalk.kakao.com/t/koe010-bad-client-credentials/115388
//             */
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
////            headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8"); //encoder error
//
//            System.out.println("4");
//
//            HttpEntity<KakaoTokenRequestDTO> httpRequestEntity = new HttpEntity<>(requestParams, headers);
//
//            System.out.println(httpRequestEntity.getBody());
//            System.out.println(httpRequestEntity.getHeaders());
//
//            System.out.println("5");
//
//            ResponseEntity<String> apiResponseJson = restTemplate.exchange(
//                    "https://kauth.kakao.com/oauth/token",
//                    HttpMethod.POST,
//                    httpRequestEntity,
//                    String.class
//            );
//
//            System.out.println(apiResponseJson);
//            System.out.println("6");
//
            System.out.println("5");
            // ObjectMapper를 통해 String to Object로 변환
            ObjectMapper objectMapper = new ObjectMapper();

            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // NULL이 아닌 값만 응답받기(NULL인 경우는 생략)

            System.out.println("7");

            KakaoTokenResponseDTO kakaoLoginResponse = objectMapper.readValue(apiResponseJson.getBody(), new TypeReference<KakaoTokenResponseDTO>() {
            });
//
//            System.out.println("8");
//
            return ResponseEntity.ok().body(kakaoLoginResponse);
//
        } catch (Exception e) {
            e.printStackTrace();
        }
//
        return ResponseEntity.badRequest().body(null);

    }



}
