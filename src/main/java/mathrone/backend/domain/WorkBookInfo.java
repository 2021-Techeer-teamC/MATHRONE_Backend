package mathrone.backend.domain;
import com.sun.istack.NotNull;
import com.vladmihalcea.hibernate.type.array.IntArrayType;
import com.vladmihalcea.hibernate.type.array.LongArrayType;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mathrone.backend.controller.dto.CarouselResponseDto;
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

    @Column(name = "thumbnail")
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
}
