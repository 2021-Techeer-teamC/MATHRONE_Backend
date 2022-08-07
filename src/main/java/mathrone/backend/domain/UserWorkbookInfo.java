package mathrone.backend.domain;

import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Entity
@Table(name = "user_workbook_rel")
public class UserWorkbookInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //JPA 사용시 필요
    @Column(name = "workbook_id")
    private String workbookId;

    @Column(name = "user_id")
    private Integer userId;

    private boolean hide;

    @Column(name = "is_vote")
    private boolean isVote;

    @Column(name = "workbook_star")
    private boolean workbookStar;

    @Column(name = "workbook_try")
    private boolean workbookTry;


    public String getWorkbookId() {
        return workbookId;
    }

    public Integer getUserId() {
        return userId;
    }

    public boolean getHide(){ return hide;}
    public boolean getIsVote(){ return isVote;}
    public boolean getWorkbookStar(){ return workbookStar;}
    public boolean getWorkbookTry(){ return workbookTry;}


}
