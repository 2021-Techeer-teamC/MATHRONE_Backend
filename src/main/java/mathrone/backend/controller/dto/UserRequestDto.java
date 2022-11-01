package mathrone.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {

    private String accountId;
    private String password;

    public UsernamePasswordAuthenticationToken of(){
        return new UsernamePasswordAuthenticationToken(accountId, password);
    }
}
