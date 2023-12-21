package mathrone.backend.controller.dto.chapter;


import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChapterGroup {

    private String group; // chapter table의 subject에서 컬럼명 변경
    private Set<ChapterDto> chapters;

    @Builder
    public ChapterGroup(String group, Set<ChapterDto> chapters) {
        this.group = group;
        this.chapters = chapters;
    }
}
