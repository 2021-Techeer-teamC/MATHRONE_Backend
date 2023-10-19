package mathrone.backend.domain;

import com.sun.istack.NotNull;
import com.vladmihalcea.hibernate.type.array.IntArrayType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TypeDef;

@NoArgsConstructor
@Entity
@Table(name = "problem")
@TypeDef(name = "int-array", typeClass = IntArrayType.class)
@Getter
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //JPA 사용시 필요
    @Column(name = "problem_id")
    private String problemId;

    @Column(name = "problem_num")
    @NotNull
    private String problemNum;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "workbook_id")
    private WorkBookInfo workbook;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "chapter_id")
    private ChapterInfo chapter;

    @Column(name = "problem_img")
    @NotNull
    private String problemImg;

    @Column(name = "level")
    private int level;

    @Column(name = "is_multiple")
    @NotNull
    private boolean multiple;

}

