package mathrone.backend.domain;


import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@NoArgsConstructor
@Entity
@Table(name = "workbook_level")
@Getter
public class WorkbookLevelInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //JPA 사용시 필요
    @Column(name = "workbook_level_id")
    private int workbookLevelId;

    @NotNull
    @Column(name = "low_cnt")
    private int lowCnt;

    @NotNull      
    @Column(name = "mid_cnt")
    private int midCnt;

    @NotNull      
    @Column(name = "high_cnt")
    private int highCnt;

    @NotNull      
    @Column(name = "workbook_id")
    private String workbookId;

    public void updateLowCount(int count){
        this.lowCnt = count;
    }

    public void updateMidCount(int count){
        this.midCnt = count;
    }

    public void updateHighCount(int count){
        this.highCnt = count;
    }

    public int mostVotedLevel(){
        if(this.lowCnt <= this.midCnt){
            if(this.midCnt <= this.highCnt)
                return 3;
            else
                return 2;
        }
        else {
            if(this.lowCnt <= this.highCnt)
                return 3;
        }
        return 1;
    }
}
