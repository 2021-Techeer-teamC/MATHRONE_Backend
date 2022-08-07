package mathrone.backend.domain;

import com.sun.istack.NotNull;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.io.Serializable;

class WorkbookRelPK implements Serializable {  // Composite Key
    private Integer userId;
    private String workbookId;
}

@NoArgsConstructor
@Entity
@IdClass(WorkbookRelPK.class)
public class UserWorkbookRelInfo {

    @Id
    @Column(name = "user_id")
    Integer userId;

    @Id
    @Column(name = "workbook_id")
    String workbookId;

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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getWorkbookId() {
        return workbookId;
    }

    public void setWorkbookId(String workbookId) {
        this.workbookId = workbookId;
    }

    public Boolean getHide() {
        return hide;
    }

    public void setHide(Boolean hide) {
        this.hide = hide;
    }

    public Boolean getVote() {
        return isVote;
    }

    public void setVote(Boolean vote) {
        isVote = vote;
    }

    public Boolean getWorkbookStar() {
        return workbookStar;
    }

    public void setWorkbookStar(Boolean workbookStar) {
        this.workbookStar = workbookStar;
    }

    public Boolean getWorkbookTry() {
        return workbookTry;
    }

    public void setWorkbookTry(Boolean workbookTry) {
        this.workbookTry = workbookTry;
    }
}
