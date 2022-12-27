package mathrone.backend.controller.dto.OauthDTO.Kakao;

import org.springframework.stereotype.Component;

@Component
public class KakaoAuthResponseDTO {

    private String code;

    private String state;

    private String error;

    private String errorDescription;

}
