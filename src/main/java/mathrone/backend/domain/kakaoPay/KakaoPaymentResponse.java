package mathrone.backend.domain.kakaoPay;


import com.google.type.DateTime;
import org.springframework.stereotype.Component;

@Component
public class KakaoPaymentResponse {


    String tid;

    String next_redirect_app_url; // 앱인경우

    String next_redirect_mobile_url; //모바일 웹일 경우

    String next_redirect_pc_url; // pc일 경우

    String android_app_scheme; //카카오 페이 결제화면으로 이동하는 안드로이드 앱 슼미

    String ios_app_scheme; //카카오 페이 결제화면으로 이동하는 ios앱 스킴

    DateTime created_at; //결제 준비 요청 시간


}
