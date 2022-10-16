package mathrone.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mathrone.backend.domain.UserInfo;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@NoArgsConstructor
public class UserSignUpDto {
    private String email;
    private String password;
    private String id;

    public UserSignUpDto(String email, String password, String id) {
        this.email = email;
        this.password = password;
        this.id = id;
    }

    public UserInfo toUser(PasswordEncoder passwordEncoder, String resType){
        return UserInfo.builder()
                .id(id)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role("ROLE_USER")
                .resType(resType)
                .build();
    }
}
