package mathrone.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {

    private String nickname;
    private String password;

    public UsernamePasswordAuthenticationToken of() {
        return new UsernamePasswordAuthenticationToken(nickname, password);
    }
}
