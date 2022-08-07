package mathrone.backend.domain;

import com.sun.istack.NotNull;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@NoArgsConstructor
public class userWorkbookData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String workbookId;

    @NotNull
    private String title;

    @NotNull
    private String img;

    @NotNull
    private String publisher;

    private String level;
    private Boolean star;

    public userWorkbookData(String workbookId, String title, String img, String publisher, String level, Boolean star){
        this.workbookId=workbookId;
        this.title=title;
        this.publisher=publisher;
        this.img=img;
        this.level=level;
        this.star=star;
    }

    public String getWorkbookId() {
        return workbookId;
    }

    public void setWorkbookId(String workbookId) {
        this.workbookId = workbookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Boolean getStar() {
        return star;
    }

    public void setStar(Boolean star) {
        this.star = star;
    }
}
