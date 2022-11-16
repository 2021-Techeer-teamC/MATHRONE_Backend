package mathrone.backend.controller.dto.OauthDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestCodeDTO {

    private String code;


    public String getCode() {
        return code;
    }
}
