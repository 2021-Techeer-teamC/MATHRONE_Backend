package mathrone.backend.domain.kakaoPay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class KakaoPaymentOAuthUtils {

    @Value("${kakao.payment.url}")
    private String kakaoPaymentUrl;

    @Value("${kakao.admin.key}")
    private String appAdminKye;

}
