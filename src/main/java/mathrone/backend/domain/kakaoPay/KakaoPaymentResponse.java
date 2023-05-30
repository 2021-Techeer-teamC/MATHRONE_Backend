package mathrone.backend.domain.kakaoPay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class KakaoPaymentResponse {


    boolean tms_result;

    String tid;

    String next_redirect_app_url; // 앱인경우

    String next_redirect_mobile_url; //모바일 웹일 경우

    String next_redirect_pc_url; // pc일 경우

    String android_app_scheme; //카카오 페이 결제화면으로 이동하는 안드로이드 앱 슼미

    String ios_app_scheme; //카카오 페이 결제화면으로 이동하는 ios앱 스킴

    LocalDateTime created_at; //결제 준비 요청 시간


}
