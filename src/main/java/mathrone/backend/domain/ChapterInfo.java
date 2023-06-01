package mathrone.backend.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Table(name = "chapter")
public class ChapterInfo {

    @Id
    @Column(name = "chapter_id")
    private String chapterId;
    private String group;
    private String name;

    public String getChapterId() {
        return chapterId;
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }
}
