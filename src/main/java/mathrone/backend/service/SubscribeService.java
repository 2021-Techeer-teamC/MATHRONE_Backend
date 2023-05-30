package mathrone.backend.service;


import mathrone.backend.domain.kakaoPay.KakaoPaymentRequest;
import mathrone.backend.domain.kakaoPay.KakaoPaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class SubscribeService {


    KakaoPayService kakaoPayService;


    SubscribeService(KakaoPayService kakaoPayService){
        this.kakaoPayService= kakaoPayService;
    }


    public KakaoPaymentResponse kakaoPaymentReady(){

        int quantity = 1;
        int price = 4900;

        KakaoPaymentRequest request = KakaoPaymentRequest.builder()
                .cid("TC0ONETIME")
                .partner_order_id("1") //주문번호?? table을 따로 만들어야하나..
                .partner_user_id("1") // 회원 번호? 회원 id하면되나
                .item_name("1달 결제")
                .quantity(quantity)
                .total_amount(price * quantity)
                .tax_free_amount(price)
                .approval_url("http://3.34.120.209:3000")
                .cancel_url("http://3.34.120.209:3000/info")
                .fail_url("http://3.34.120.209:3000/books")
                .build();

        KakaoPaymentResponse kakaoPaymentResponse = kakaoPayService.readyForKakaoPayment(request);

        System.out.println(kakaoPaymentResponse.getTid());
        System.out.println(kakaoPaymentResponse.getAndroid_app_scheme());
        System.out.println(kakaoPaymentResponse.getCreated_at());
        System.out.println(kakaoPaymentResponse.getIos_app_scheme());
        System.out.println(kakaoPaymentResponse.getNext_redirect_app_url());
        System.out.println(kakaoPaymentResponse.getNext_redirect_pc_url());

        return kakaoPaymentResponse;

    }



}
