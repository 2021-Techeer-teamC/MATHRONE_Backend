package mathrone.backend.domain;

import com.sun.istack.NotNull;
import com.vladmihalcea.hibernate.type.array.IntArrayType;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@NoArgsConstructor
@Entity
@Table(name = "problem")
@TypeDef(name = "int-array", typeClass = IntArrayType.class)
@Getter
@Setter
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //JPA 사용시 필요
    @Column(name = "problem_id")
    private String problemId;

    @Column(name = "problem_num")
    @NotNull
    private String problemNum;

    @Column(name = "chapter_id")
    @NotNull
    private String chapterId;

    @Column(name = "workbook_id")
    @NotNull
    private String workbookId;

    @Column(name = "problem_img")
    @NotNull
    private String problemImg;

    @Column(name = "level_of_diff")
    private int levelOfDiff;

//    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true) //영속화 설정
//    private List<ProblemTry> problemTryList = new LinkedList<>();   // null 에러 방지


//    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true) //영속화 설정
//    private List<ProblemTry> problemTryList = new LinkedList<>();   // null 에러 방지


    public String getProblemId() {
        return problemId;
    }

    public String getProblemNum() {
        return problemNum;
    }

    public String getChapterId() {
        return chapterId;
    }

    public String getWorkbookId() {
        return workbookId;
    }

    public String getProblemImg() {
        return problemImg;
    }

    public int getLevelOfDiff() {
        return levelOfDiff;
    }
}
