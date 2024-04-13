package mathrone.backend.domain;

import com.sun.istack.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Getter
public class UserProfile {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) //JPA 사용시 필요)
    @Column(name = "user_id")
    private int userId;

    @NotNull
    private String nickname;

    @NotNull
    private String password;


    private String profileImg;

    @NotNull
    private int exp = 0;

    @NotNull
    private boolean premium;

    @NotNull
    private String email;

    private String phoneNum;

    private UserRank rankInfo;

    private boolean Premium;

    private SubscriptionInfo subscription;


    public UserProfile(int userId, String nickname, String password, String profileImg, int exp, String email, String phoneNum, UserRank rankInfo, boolean premium ,SubscriptionInfo subscription) {
        this.userId = userId;
        this.nickname = nickname;
        this.password = password;
        this.profileImg = profileImg;
        this.exp = exp;
        this.email = email;
        this.phoneNum = phoneNum;
        this.rankInfo = rankInfo;
        this.premium=premium;
        this.subscription = subscription;
    }
}
