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
    private String id;

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


    private String userImg;

    private String role;

    private UserRank rankInfo;


    public UserProfile(int userId, String id, String password, String profileImg, int exp, boolean premium, String email, String phoneNum, String userImg, String role, UserRank rankInfo) {
        this.userId = userId;
        this.id = id;
        this.password = password;
        this.profileImg = profileImg;
        this.exp = exp;
        this.premium = premium;
        this.email = email;
        this.phoneNum = phoneNum;
        this.userImg = userImg;
        this.role = role;
        this.rankInfo = rankInfo;
    }

}
