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
    Boolean hide;

    @NotNull
    @Column(name = "is_vote")
    Boolean isVote;

    @NotNull
    @Column(name = "workbook_star")
    Boolean workbookStar;

    @NotNull
    @Column(name = "workbook_try")
    Boolean workbookTry;

}
