package mathrone.backend.domain;

import com.sun.istack.NotNull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * user 중에서 문제 풀이를 시도한 workbook list를 저장하는 class
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@IdClass(WorkbookRelPK.class)
@Getter
@Table(name = "user_workbook_rel")
@Builder
@AllArgsConstructor
public class UserWorkbookRelInfo {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserInfo user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workbook_id")
    private WorkBookInfo workbook;

    @NotNull
    @Builder.Default()
    Boolean hide = false;

    @NotNull
    @Column(name = "vote_level")
    @Builder.Default()
    Integer voteLevel = 0;

    @NotNull
    @Column(name = "workbook_star")
    @Builder.Default()
    Boolean workbookStar = false;

    @NotNull
    @Column(name = "workbook_try")
    @Builder.Default()
    Boolean workbookTry = false;

    public void updateStar(boolean star) {
        this.workbookStar = star;
    }

    public void updateVote(int level){
        this.voteLevel = level;
    }

}
