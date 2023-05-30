package mathrone.backend.controller;


import com.fasterxml.jackson.databind.node.ArrayNode;
import mathrone.backend.domain.kakaoPay.KakaoPaymentResponse;
import mathrone.backend.service.SubscribeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscribe")
public class SubscribeController {


    SubscribeService subscribeService;

    SubscribeController(SubscribeService subscribeService){
        this.subscribeService = subscribeService;
    }


    @PostMapping("/tmp") // 상위 랭킹 정보를 가져옴
    public KakaoPaymentResponse getRank(){
        return subscribeService.kakaoPaymentReady();
    }


}
