package mathrone.backend.controller.dto;

import com.sun.istack.NotNull;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Getter
public class WorkbookDto {

    //bookItem에 필요한 attribute
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //JPA 사용시 필요
    private String workbookId;

    @NotNull
    private String title;

    @NotNull
    private String publisher;

    @NotNull
    private String thumbnail;

    private String level;
    private boolean star;

    //생성자
    public WorkbookDto(String workbookId, String title, String publisher, String thumbnail,
        String level, boolean star) {
        this.workbookId = workbookId;
        this.title = title;
        this.publisher = publisher;
        this.thumbnail = thumbnail;
        this.level = level;
        this.star = star;
    }

}
