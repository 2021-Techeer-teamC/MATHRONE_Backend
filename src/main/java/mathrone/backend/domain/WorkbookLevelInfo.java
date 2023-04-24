package mathrone.backend.domain;


import com.sun.istack.NotNull;
import lombok.NoArgsConstructor;
import lombok.Getter;
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

}
