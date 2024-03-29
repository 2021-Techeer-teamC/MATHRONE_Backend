package mathrone.backend.controller.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class FindDto {

    @Email @NotBlank
    String email;

    @NotBlank
    String nickname;
}
