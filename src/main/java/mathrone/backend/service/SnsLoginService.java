
package mathrone.backend.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import mathrone.backend.controller.dto.OauthDTO.GoogleIDToken;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoIDToken;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


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


            System.out.println("2");

        // 해더 만들기
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");


            System.out.println("3");

        // 바디 만들기 (HashMap 사용 불가!)
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoOAuthLoginUtils.getClientId());
        params.add("redirect_uri", kakaoOAuthLoginUtils.getKakaoRedirectUri());
        params.add("client_secret", kakaoOAuthLoginUtils.getClientSecret());
        params.add("code", code);


            System.out.println("4");

        // 해더와 바디를 하나의 오브젝트로 만들기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(params, headers);


            System.out.println("5");

        // Http 요청하고 리턴값을 response 변수로 받기
        ResponseEntity<String> apiResponseJson = rt.exchange(
                "https://kauth.kakao.com/oauth/token", // Host
                HttpMethod.POST, // Request Method
                kakaoTokenRequest,	// RequestBody
                String.class
        );	// return Object


            System.out.println("6");

            // ObjectMapper를 통해 String to Object로 변환
            ObjectMapper objectMapper = new ObjectMapper();

            System.out.println("7");

            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

            System.out.println("8");

            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // NULL이 아닌 값만 응답받기(NULL인 경우는 생략)

            System.out.println("9");

            KakaoTokenResponseDTO kakaoLoginResponse = objectMapper.readValue(apiResponseJson.getBody(), new TypeReference<KakaoTokenResponseDTO>() {
            });

            System.out.println("10");
            System.out.println(kakaoLoginResponse.getId_token());
            System.out.println(kakaoLoginResponse.getAccess_token());

            return ResponseEntity.ok().body(kakaoLoginResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.badRequest().body(null);

    }


    public ResponseEntity<KakaoIDToken> decodeIdToken(String idToken) throws JsonProcessingException {

        System.out.println(idToken);
        Map<String, Object> map = new HashMap<String, Object>();
        System.out.println("1");
        //1. ID토큰을 온점(.)을 기준으로 헤더,페이로드,서명을 분리
//        Map<String, Object> map = new HashMap<String, Object>();
        String[] params = idToken.split("\\."); //escape 필수

        System.out.println("2");

        for(int i=0;i<3;i++){
            System.out.println("???");
            System.out.println(params[i]);
        }

        System.out.println("3");
        //2. 페이로드를 Base64방식으로 디코드
        Base64.Decoder decoder = Base64.getDecoder();
        String payload = new String(decoder.decode(params[1])); //0 : header / 1 : payload / 2 : signature

        System.out.println("4");
        //3.
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> returnMap = mapper.readValue(payload, Map.class);

        for( Map.Entry<String, Object> entry : returnMap.entrySet() ){
            String strKey = entry.getKey();
            String strValue = String.valueOf(entry.getValue());
            System.out.println( strKey +":"+ strValue );
        }



//
//        for(String param : params){
//            String name = param.split("=")[0];
//            String value = param.split("=")[1];
//            map.put(name, value);
//        }
//
//        String token = MapUtils.getString(map, "id_token");
//        String[] check = token.split("\\.");
//        Base64.Decoder decoder = Base64.getDecoder();
//        String payload = new String(decoder.decode(check[1]));
//
//        ObjectMapper mapper = new ObjectMapper();
//        Map<String, Object> returnMap = mapper.readValue(payload, Map.class);

        return null;

    }



}
