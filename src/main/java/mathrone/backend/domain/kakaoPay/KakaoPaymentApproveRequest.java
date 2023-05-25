package mathrone.backend.domain.kakaoPay;

import org.springframework.stereotype.Component;

@Component
public class KakaoPaymentApproveRequest {

    String cid;

//    String cid_secret;

    String tid; //결제 고유번호

    String partner_order_id;

    String partner_user_id;

    String pg_token;

//    String payload;

//    String total_amount;


}
