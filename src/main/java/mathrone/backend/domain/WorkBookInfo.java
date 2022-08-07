package mathrone.backend.domain;
import com.sun.istack.NotNull;
import com.vladmihalcea.hibernate.type.array.IntArrayType;
import lombok.NoArgsConstructor;
import mathrone.backend.controller.dto.CarouselResponseDto;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@NoArgsConstructor
@Entity
@Table(name = "workbook")
@TypeDef(name = "int-array", typeClass = IntArrayType.class)
public class WorkBookInfo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) //JPA 사용시 필요
    @Column(name = "workbook_id")
    private String workbookId;

    @NotNull
    private String title;

    @NotNull
    private String publisher;

    @Column(name = "profile_img")
    @NotNull
    private String profileImg;

    @NotNull
    private String content;

    @NotNull
    private String type;

    private Short year;

    private Short month;

    @Column(name="chapter_id")
    @Type(type = "int-array")
    private Integer[] chapterId;

    private String category;

    public String getWorkbookId() {
        return workbookId;
    }

    public String getTitle() {
        return title;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public Short getYear() {
        return year;
    }

    public Short getMonth() {
        return month;
    }

    public Integer[] getChapter_id() {
        return chapterId;
    }

    public String getCategory(){return category;}
}
