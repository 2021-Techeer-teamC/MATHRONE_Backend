package mathrone.backend.domain.kakaoPay;



public class CardInfo {

    String purchase_corp;	//String	매입 카드사 한글명
    String purchase_corp_code;	//String	매입 카드사 코드
    String issuer_corp;	//String	카드 발급사 한글명
    String issuer_corp_code;	//String	카드 발급사 코드
    String kakaopay_purchase_corp;	//String	카카오페이 매입사명
    String kakaopay_purchase_corp_code;	//String	카카오페이 매입사 코드
    String kakaopay_issuer_corp;	//String	카카오페이 발급사명
    String kakaopay_issuer_corp_code;	//String	카카오페이 발급사 코드
    String bin;	//String	카드 BIN
    String card_type;	//String	카드 타입
    String install_month;	//String	할부 개월 수
    String approved_id;	//String	카드사 승인번호
    String card_mid;	//String	카드사 가맹점 번호
    String interest_free_install;	//String	무이자할부 여부(Y/N)
    String card_item_cod;	//String	카드 상품 코드

}
