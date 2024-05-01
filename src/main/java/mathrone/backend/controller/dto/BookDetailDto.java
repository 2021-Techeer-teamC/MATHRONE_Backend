package mathrone.backend.controller.dto;

import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import mathrone.backend.controller.dto.chapter.ChapterGroup;
import mathrone.backend.domain.Tag;

@Getter
@AllArgsConstructor
@Builder
public class BookDetailDto {

    private String workbookId;
    private String title;
    private String summary; // 추가?
    private String publisher;
    private String category;
    private String thumbnail; // workbook table profile_img에서 컬럼명 변경
    private String content;
    private String type;
    private Short year;
    private Short month;
    private Boolean star;
    private Set<ChapterGroup> chapterGroup;
    private List<Tag> tags;
    private LevelInfoDto level;

}
