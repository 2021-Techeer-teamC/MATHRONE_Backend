package mathrone.backend.domain.kakaoPay;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoPayApproveRequest {


    String tid;

    String pgToken;
}
