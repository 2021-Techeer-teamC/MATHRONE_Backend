package mathrone.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeProfileDto {

    private String nickname;               // 닉네임
    private String profileImg;              // 이미지 링크
    private String phoneNum;

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }
}   // 프로필 수정에 필요한 데이터

