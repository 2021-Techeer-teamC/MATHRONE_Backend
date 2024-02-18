package mathrone.backend.controller.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mathrone.backend.domain.UserInfo;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@NoArgsConstructor
public class UserSignUpDto {

    private String nickname;
    private String password;
    private String email;   // email에서 id로 바뀜에 따라, 필요한지 여부 토의하기
    private String emailVerifyCode; //이메일 인증코드 함께 전송

    public UserSignUpDto(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    public UserInfo toUser(PasswordEncoder passwordEncoder, String resType) {
        return UserInfo.builder()
            .nickname(nickname)
            .email(email)
            .password(passwordEncoder.encode(password))
            .role("ROLE_USER")
            .resType(resType)
            .build();
    }
}
