package mathrone.backend.controller.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class FindNicknameDto {

    @Email @NotBlank
    String email;
}
