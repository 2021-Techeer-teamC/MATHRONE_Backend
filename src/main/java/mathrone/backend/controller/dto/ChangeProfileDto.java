package mathrone.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeProfileDto {

    private String nickname;               // 닉네임
    private String phoneNum;

}   // 프로필 수정에 필요한 데이터

