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
    private String subject;
    private String chapter;

    public String getChapterId() {
        return chapterId;
    }

    public String getSubject() {
        return subject;
    }

    public String getChapter() {
        return chapter;
    }
}
