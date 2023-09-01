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
public class KakaoPaymentRequest {

    String cid;

//    String cid_secret;

    String partner_order_id;

    String partner_user_id;

    String item_name;

//    String item_code;

    Integer quantity;

    Integer total_amount;

    Integer tax_free_amount;

//    Integer var_amount;

//    Integer green_deposit;

    String approval_url; //결제 성공 시 redirect url;

    String cancel_url; // 결제 취소 시

    String fail_url; // 결제 실패 시

//    JsonArray available_cards;  결제가능 카드 제한 시 -> 카카오페이와 사전협의 필요

    String payment_method_type; //결제 수단(CARD/MONEY)

    Integer install_month; //카드 할부 0~12개월



}
