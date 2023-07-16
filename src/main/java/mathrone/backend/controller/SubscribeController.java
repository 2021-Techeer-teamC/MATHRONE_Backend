package mathrone.backend.controller;


import com.fasterxml.jackson.databind.node.ArrayNode;
import mathrone.backend.domain.KakaoPayApproveRequest;
import mathrone.backend.domain.KakaoPayRequestResponse;
import mathrone.backend.domain.kakaoPay.KakaoPaymentApproveResponse;
import mathrone.backend.domain.kakaoPay.KakaoPaymentResponse;
import mathrone.backend.service.SubscribeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/subscribe")
public class SubscribeController {


    SubscribeService subscribeService;

    SubscribeController(SubscribeService subscribeService){
        this.subscribeService = subscribeService;
    }


    @PostMapping("/pay-request") // 결제 요청
    public KakaoPayRequestResponse kakaoPayRequest(HttpServletRequest request){

        return  subscribeService.kakaoPaymentReady(request);

    }


    @PostMapping("/pay-approve") // 결제 승인
    public KakaoPaymentApproveResponse kakaoPayApprove(KakaoPayApproveRequest kakaoPayApproveRequest){

        return  subscribeService.kakaoPaymentApprove(kakaoPayApproveRequest);

    }


    @PostMapping("/pay-error") // 결제 에러
    public void kakaoPayError(String tid){

        subscribeService.kakaoPaymentNotApproved(tid, "ERROR");

    }


    @PostMapping("/pay-canceled") // 결제 취소
    public void kakaoPayCanceled(String tid){

        subscribeService.kakaoPaymentNotApproved(tid,"CANCELED");

    }
}
