package mathrone.backend.domain;

import com.sun.istack.NotNull;
import com.vladmihalcea.hibernate.type.array.IntArrayType;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@NoArgsConstructor
@Entity
public class bookItem {

    //bookItem에 필요한 attribute
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //JPA 사용시 필요
    private String workbookId;

    @NotNull
    private String title;

    @NotNull
    private String publisher;

    @NotNull
    private String profileImg;

    private String level;
    private Long star;

    //생성자
    public bookItem(String workbookId, String title, String publisher, String profileImg, String level, Long star){
        this.workbookId=workbookId;
        this.title=title;
        this.publisher=publisher;
        this.profileImg=profileImg;
        this.level=level;
        this.star=star;
    }

    //getter & setter
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

    public String getLevel() {return level;}

    public Long getStar() {return star;}


    public void setWorkbookId(String workbookId) {
        this.workbookId=workbookId;
    }

    public void setTitle(String title) {
        this.title=title;
    }

    public void setPublisher(String publisher) {
        this.publisher=publisher;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg=profileImg;
    }

    public void setLevel(String level) { this.level=level;}

    public void setStar(Long star) { this.star=star;}

}
