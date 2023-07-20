package mathrone.backend.domain;
import com.sun.istack.NotNull;
import com.vladmihalcea.hibernate.type.array.LongArrayType;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@NoArgsConstructor
@Entity
@Table(name = "workbook")
@TypeDef(name = "String-array", typeClass = StringArrayType.class)
@TypeDef(name = "Long-array", typeClass = LongArrayType.class)
@Getter
public class WorkBookInfo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) //JPA 사용시 필요
    @Column(name = "workbook_id")
    private String workbookId;

    @NotNull
    private String title;

    @NotNull
    private String publisher;

    @NotNull
    private String thumbnail;

    @NotNull
    private String content;

    @NotNull
    private String type;

    private Short year;

    private Short month;

    @Column(name="chapter_id")
    @Type(type = "String-array")
    private String[] chapterId;

    @Type(type = "Long-array")
    private Long[] tags;

    private String category;

    @OneToMany(
        mappedBy = "workbook",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY)
    private List<UserWorkbookRelInfo> userWorkbookRelInfo = new LinkedList<>();
}
