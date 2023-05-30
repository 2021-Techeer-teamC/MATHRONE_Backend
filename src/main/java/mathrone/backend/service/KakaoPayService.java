package mathrone.backend.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoTokenResponseDTO;
import mathrone.backend.domain.kakaoPay.KakaoPaymentOAuthUtils;
import mathrone.backend.domain.kakaoPay.KakaoPaymentRequest;
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
public class KakaoPayService {


    private final KakaoPaymentOAuthUtils kakaoPaymentOAuthUtils;


    KakaoPayService(KakaoPaymentOAuthUtils kakaoPaymentOAuthUtils){
        this.kakaoPaymentOAuthUtils= kakaoPaymentOAuthUtils;
    }

    public KakaoPaymentResponse readyForKakaoPayment(KakaoPaymentRequest kakaoPaymentRequest){

        try {

            RestTemplate rt = new RestTemplate();
            rt.setRequestFactory(
                    new HttpComponentsClientHttpRequestFactory()); //error message type및 description확인 가능

            // 해더 만들기
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
            headers.add("Authorization", "KakaoAK "+ kakaoPaymentOAuthUtils.getAppAdminKye());

            // 바디 만들기 (HashMap 사용 불가!)
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.add("cid", kakaoPaymentRequest.getCid());
            params.add("partner_order_id", kakaoPaymentRequest.getPartner_order_id());
            params.add("partner_user_id", kakaoPaymentRequest.getPartner_user_id());
            params.add("item_name", kakaoPaymentRequest.getItem_name());
            params.add("quantity", kakaoPaymentRequest.getQuantity());
            params.add("total_amount", kakaoPaymentRequest.getTotal_amount());
            params.add("tax_free_amount", kakaoPaymentRequest.getQuantity());
            params.add("approval_url", kakaoPaymentRequest.getApproval_url());
            params.add("cancel_url", kakaoPaymentRequest.getCancel_url());
            params.add("fail_url", kakaoPaymentRequest.getFail_url());

            // 해더와 바디를 하나의 오브젝트로 만들기
            HttpEntity<MultiValueMap<String, Object>> kakaoTokenRequest =
                    new HttpEntity<>(params, headers);

            // Http 요청하고 리턴값을 response 변수로 받기
            ResponseEntity<String> apiResponseJson = rt.exchange(
                    "https://kapi.kakao.com/v1/payment/ready", // Host
                    HttpMethod.POST, // Request Method
                    kakaoTokenRequest,    // RequestBody
                    String.class
            );    // return Object


            System.out.println(apiResponseJson.toString());
            System.out.println(apiResponseJson.getBody().toString());

            // ObjectMapper를 통해 String to Object로 변환
            ObjectMapper objectMapper = new ObjectMapper();

            /*
            com.fasterxml.jackson.databind.exc.InvalidDefinitionException: Java 8 date/time type `java.time.LocalDateTime` not supported by default: add Module "com.fasterxml.jackson.datatype:jackson-datatype-jsr310" to enable handling
             */

            objectMapper.registerModule(new JavaTimeModule());

            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

            objectMapper.setSerializationInclusion(
                    JsonInclude.Include.NON_NULL); // NULL이 아닌 값만 응답받기(NULL인 경우는 생략)

            KakaoPaymentResponse kakaoPaymentResponse = objectMapper.readValue(
                    apiResponseJson.getBody(), new TypeReference<KakaoPaymentResponse>() {
                    });

            return kakaoPaymentResponse;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;


    }


}
