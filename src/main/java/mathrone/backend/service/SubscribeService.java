package mathrone.backend.service;


import mathrone.backend.domain.kakaoPay.KakaoPayApproveRequest;
import mathrone.backend.domain.kakaoPay.KakaoPayRequestResponse;
import mathrone.backend.domain.Subscription;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.domain.kakaoPay.KakaoPaymentApproveRequest;
import mathrone.backend.domain.kakaoPay.KakaoPaymentApproveResponse;
import mathrone.backend.domain.kakaoPay.KakaoPaymentRequest;
import mathrone.backend.domain.kakaoPay.KakaoPaymentResponse;
import mathrone.backend.error.exception.CustomException;
import mathrone.backend.repository.SubscriptionRepository;
import mathrone.backend.repository.UserInfoRepository;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

import static mathrone.backend.error.exception.ErrorCode.SUBSCRIPTION_ERROR_ALREADY_SUBSCRIBED;

@Service
public class SubscribeService {


    KakaoPayService kakaoPayService;
    SubscriptionRepository subscriptionRepository;
    UserInfoRepository userInfoRepository;
    AuthService authService;

    SubscribeService(KakaoPayService kakaoPayService, SubscriptionRepository subscriptionRepository, AuthService authService, UserInfoRepository userInfoRepository){
        this.subscriptionRepository= subscriptionRepository;
        this.kakaoPayService= kakaoPayService;
        this.authService = authService;
        this.userInfoRepository= userInfoRepository;
    }


    public Subscription createSubscription(String item, int userId, int price){

        Subscription newSub = Subscription.builder()
                .item(item)
                .userId(userId)
                .price(price)
                .build();


        Subscription s = subscriptionRepository.save(newSub);


        return s;

    }




    public KakaoPayRequestResponse kakaoPaymentReady(HttpServletRequest payRequest){

        int quantity = 1;
        int price = 4900;
        String item = "1달 구독";
        String cid = "TC0ONETIME";


        UserInfo u = authService.findUserFromRequest(payRequest);

        /*
        이미 이번달에 구독이미 되어있는 사람은 더 구독못하게 막기
         */

        if(u.isPremium()){ ///일단 런타임 에러로 두고 나중에 에러코드 생기면 그떄 수저할 것
            throw new CustomException(SUBSCRIPTION_ERROR_ALREADY_SUBSCRIBED);
        }


        //프리미엄 이미 한사람 리턴 전에 데베에 저장해버리니까 영원히 팬딩상태로 데베에 쌓이기만해서 밑으로 내려야함
        Subscription s = createSubscription(item, u.getUserId(), price);


        String orderId = Integer.toString(s.getSubId());
        String userId = Integer.toString(u.getUserId());

        KakaoPaymentRequest request = KakaoPaymentRequest.builder()
                .cid(cid)
                .partner_order_id(orderId) //주문번호
                .partner_user_id(userId) // 회원 번호
                .item_name(item)
                .quantity(quantity)
                .total_amount(price * quantity)
                .tax_free_amount(price)
                .approval_url("http://localhost:3000/payment/success")
                .cancel_url("http://localhost:3000/payment/cancel")
                .fail_url("http://localhost:3000/payment/fail")
                .build();

        KakaoPaymentResponse kakaoPaymentResponse = kakaoPayService.readyForKakaoPayment(request);


        Subscription updatedSub = s.updateTid(kakaoPaymentResponse.getTid());

        //TID update
        subscriptionRepository.save(updatedSub);


//        System.out.println(kakaoPaymentResponse.getTid());
//        System.out.println(kakaoPaymentResponse.getAndroid_app_scheme());
//        System.out.println(kakaoPaymentResponse.getCreated_at());
//        System.out.println(kakaoPaymentResponse.getIos_app_scheme());
//        System.out.println(kakaoPaymentResponse.getNext_redirect_app_url());
//        System.out.println(kakaoPaymentResponse.getNext_redirect_pc_url());
//        System.out.println(kakaoPaymentResponse.getNext_redirect_mobile_url());




        return KakaoPayRequestResponse.builder()
                .tid(updatedSub.getTid())
                .pcUrl(kakaoPaymentResponse.getNext_redirect_pc_url())
                .mobileUrl(kakaoPaymentResponse.getNext_redirect_mobile_url())
                .build();

    }



    public KakaoPaymentApproveResponse kakaoPaymentApprove(KakaoPayApproveRequest kakaoPayApproveRequest){


        String cid = "TC0ONETIME";

        Subscription s = subscriptionRepository.findByTid(kakaoPayApproveRequest.getTid()).orElseThrow();


        KakaoPaymentApproveRequest kakaoPaymentApproveRequest = KakaoPaymentApproveRequest.builder()
                 .tid(kakaoPayApproveRequest.getTid())
                 .pg_token(kakaoPayApproveRequest.getPgToken())
                 .cid(cid)
                 .partner_order_id(Integer.toString(s.getSubId()))
                 .partner_user_id(Integer.toString(s.getUserId()))
                 .build();


        KakaoPaymentApproveResponse kakaoPaymentApproveResponse = kakaoPayService.approveForKakaoPayment(kakaoPaymentApproveRequest);

        //상태 업데이트
        Subscription updatedSub = s.updateStatus("COMPLETE");
        subscriptionRepository.save(updatedSub);

        // 유저 프리미엄 true로 변경
        UserInfo u = userInfoRepository.findByUserId(s.getUserId());
        UserInfo updatedUser = u.updatePremium(true);
        userInfoRepository.save(updatedUser);

        return kakaoPaymentApproveResponse;

    }


    public void kakaoPaymentNotApproved(String tid, String status){

        Subscription s = subscriptionRepository.findByTid(tid).orElseThrow();

        //상태 업데이트
        Subscription updatedSub = s.updateStatus(status);
        subscriptionRepository.save(updatedSub);

    }
}
