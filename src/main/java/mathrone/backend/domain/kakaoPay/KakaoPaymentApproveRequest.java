package mathrone.backend.domain.kakaoPay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class KakaoPaymentApproveRequest {

    String cid; //가맹점코드

//    String cid_secret;

    String tid; //결제 고유번호

    String partner_order_id;

    String partner_user_id;

    String pg_token;

//    String payload;

//    String total_amount;


}
