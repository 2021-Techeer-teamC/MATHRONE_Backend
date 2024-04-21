package mathrone.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class EmailVerifyDto {
    private String accountId;
    private String code;
}
