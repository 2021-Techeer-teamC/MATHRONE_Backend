package mathrone.backend.domain.kakaoPay;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoPayRequestResponse {

    String tid;

    String pcUrl;

    String mobileUrl;

}
