package mathrone.backend.controller.dto;

import javax.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ChangePasswordDto {

    @NotBlank
    String newPassword;
}
