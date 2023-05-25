package mathrone.backend.domain.kakaoPay;

import org.springframework.stereotype.Component;

@Component
public class KakaoPaymentApproveResponse {

    String aid;		//요청 고유 번호
    String tid;	//결제 고유 번호
    String cid;	//가맹점 코드
    String sid;	//정기결제용 ID, 정기결제 CID로 단건결제 요청 시 발급
    String partner_order_id;	//가맹점 주문번호, 최대 100자
    String partner_user_id;	//가맹점 회원 id, 최대 100자
    String payment_method_type;	//결제 수단, CARD 또는 MONEY 중 하나
    Amount amount;	//결제 금액 정보
    CardInfo card_info;	//결제 상세 정보, 결제수단이 카드일 경우만 포함
    String item_name;	//상품 이름, 최대 100자
    String item_code;	//상품 코드, 최대 100자
    Integer quantity;	//상품 수량
    Datetime created_at;	//결제 준비 요청 시각
    Datetime approved_at;	//결제 승인 시각
    String payload;//	결제 승인 요청에 대해 저장한 값, 요청 시 전달된 내용


}
