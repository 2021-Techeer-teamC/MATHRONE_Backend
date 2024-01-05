package mathrone.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mathrone.backend.domain.UserInfo;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDto {

    private String accountId;
    private String userId;

    public static UserResponseDto of(UserInfo userInfo) {
        return new UserResponseDto(userInfo.getNickname(), Integer.toString(userInfo.getUserId()));
    }
}
