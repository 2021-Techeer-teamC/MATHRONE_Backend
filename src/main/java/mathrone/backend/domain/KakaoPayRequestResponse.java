package mathrone.backend.domain;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoPayRequestResponse {

    String tid;

    String pcUrl;

    String mobileUrl;

}
