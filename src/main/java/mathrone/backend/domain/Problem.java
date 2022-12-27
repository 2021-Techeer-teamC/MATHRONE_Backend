package mathrone.backend.domain;

import com.sun.istack.NotNull;
import com.vladmihalcea.hibernate.type.array.IntArrayType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mathrone.backend.controller.dto.UserProblemTryDto;
import org.hibernate.annotations.TypeDef;

@SqlResultSetMapping(
    name = "problemTryDTOMapping",
    classes = @ConstructorResult(
        targetClass = UserProblemTryDto.class,
        columns = {
            @ColumnResult(name = "problemId", type = String.class),
            @ColumnResult(name = "problemNum", type = Integer.class),
            @ColumnResult(name = "chapterId", type = String.class),
            @ColumnResult(name = "workbookId", type = String.class),
            @ColumnResult(name = "levelOfDiff", type = Integer.class),
            @ColumnResult(name = "iscorrect", type = Boolean.class),
            @ColumnResult(name = "title", type = String.class)
        }
    )
)

@NamedNativeQuery(name = "Problem.findUserTryProblem",
    resultClass = UserProblemTryDto.class,
    resultSetMapping = "problemTryDTOMapping",
    query = "select P.problem_id as problemId,"
        + "P.problem_num as problemNum,"
        + "P.chapter_id as chapterId,"
        + "P.workbook_id as workbookId,"
        + "P.level_of_diff as levelOfDiff,"
        + "PT.iscorrect, W.title "
        + "from problem P, problem_try PT, workbook W "
        + "where P.problem_id = PT.problem_id "
        + "and P.workbook_id = W.workbook_id "
        + "and PT.user_id = :userId"

)

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

