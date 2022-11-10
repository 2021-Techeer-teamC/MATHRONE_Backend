package mathrone.backend.controller.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import mathrone.backend.domain.UserInfo;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@NoArgsConstructor
public class UserSignUpDto {
    private String accountId;
    private String password;
    private String email;   // email에서 id로 바뀜에 따라, 필요한지 여부 토의하기

    public UserSignUpDto(String email, String password, String accountId) {
        this.email = email;
        this.password = password;
        this.accountId = accountId;
    }

    public UserInfo toUser(PasswordEncoder passwordEncoder, String resType){
        return UserInfo.builder()
                .accountId(accountId)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role("ROLE_USER")
                .resType(resType)
                .build();
    }
}
