package mathrone.backend.service;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoOAuthLoginUtils;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoTokenResponseDTO;
import mathrone.backend.controller.dto.OauthDTO.OAuthLoginUtils;
import mathrone.backend.domain.kakaoPay.KakaoPaymentResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentService {


    private final KakaoOAuthLoginUtils kakaoOAuthLoginUtils;

    PaymentService(KakaoOAuthLoginUtils kakaoOAuthLoginUtils) {
        this.kakaoOAuthLoginUtils = kakaoOAuthLoginUtils;
    }

    //kakao token얻기
    public ResponseEntity<KakaoPaymentResponse> getKakaoToken(String code)
            throws JsonProcessingException {

        try {

            RestTemplate rt = new RestTemplate();
            rt.setRequestFactory(
                    new HttpComponentsClientHttpRequestFactory()); //error message type및 description확인 가능

            // 해더 만들기
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
            headers.add("Authorization", kakaoOAuthLoginUtils.getAdminKey());

            // 바디 만들기 (HashMap 사용 불가!)
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("cid", "MATHRONE");
            params.add("partner_order_id", ""); //주문번호
            params.add("partner_user_id", ""); //user_id
            params.add("item_name", "프리미엄 결제");
            params.add("quantity", "1");
            params.add("total_amount", "4900");
            params.add("tax_free_amount", "4900");
            params.add("approval_url", "http://localhost:3000");
            params.add("cancel_url", "http://localhost:");
            params.add("fail_url", "");
            params.add("payment_method_type", "CARD");


            // 해더와 바디를 하나의 오브젝트로 만들기
            HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                    new HttpEntity<>(params, headers);

            // Http 요청하고 리턴값을 response 변수로 받기
            ResponseEntity<String> apiResponseJson = rt.exchange(
                    "https://kauth.kakao.com/v1/payment/ready", // Host
                    HttpMethod.POST, // Request Method
                    kakaoTokenRequest,    // RequestBody
                    String.class
            );    // return Object

            // ObjectMapper를 통해 String to Object로 변환
            ObjectMapper objectMapper = new ObjectMapper();

            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

            objectMapper.setSerializationInclusion(
                    JsonInclude.Include.NON_NULL); // NULL이 아닌 값만 응답받기(NULL인 경우는 생략)

            KakaoTokenResponseDTO kakaoLoginResponse = objectMapper.readValue(
                    apiResponseJson.getBody(), new TypeReference<KakaoTokenResponseDTO>() {
                    });

            return ResponseEntity.ok().body(kakaoLoginResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.badRequest().body(null);

    }

}
