package mathrone.backend.domain;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoPayApproveRequest {


    String tid;

    String pgToken;
}
