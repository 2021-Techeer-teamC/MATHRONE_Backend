package mathrone.backend.domain;

import com.sun.istack.NotNull;
import com.vladmihalcea.hibernate.type.array.IntArrayType;
import java.util.LinkedList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@NoArgsConstructor
@Entity
@Table(name = "user_info")
@TypeDef(name = "int-array", typeClass = IntArrayType.class)
@Getter
@Setter
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //JPA 사용시 필요)
    @Column(name = "user_id")
    private int userId;

    @NotNull
    @Column(name = "account_id")
    private String accountId;

    @NotNull
    private String password;

    @Column(name = "profile_img")
    private String profileImg;

    @NotNull
    private int exp = 0;

    @NotNull
    private boolean premium;

    @NotNull
    private String email;

    @Column(name = "phone_num")
    private String phoneNum;

    @Column(name = "user_img")
    private String userImg;

    private String role;

    @Column(name = "register_type")
    private String resType;

    private boolean activate = true;

    @OneToMany(
        mappedBy = "user",
        cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserWorkbookRelInfo> userWorkbookRelInfo = new LinkedList<>();

    @Builder
    public UserInfo(String email, String password, String role, String accountId, String resType) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.accountId = accountId;
        this.resType = resType;
    }

    //업데이트 될만한 거
    public UserInfo updateImg(String userImg) {
        this.userImg = userImg;
        return this;
    }

    public UserInfo updatePremium(boolean premium){
        this.premium = premium;
        return this;
    }


    //accountID변경
    public UserInfo updateAccountId(String accountId){
        this.accountId = accountId;

        return this;
    }

    public UserInfo changePassword(PasswordEncoder passwordEncoder, String password) {
        this.password = passwordEncoder.encode(password);

        return this;
    }


    //탈퇴/복구 시 유저 deactivate or active again
    public UserInfo updateActivate(Boolean activate){
        this.activate=activate;

        return this;
    }

}
