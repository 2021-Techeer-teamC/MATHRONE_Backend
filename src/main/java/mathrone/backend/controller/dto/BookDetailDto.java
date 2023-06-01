package mathrone.backend.controller.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
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
    private List<ChapterGroup> chapterGroup;
    private List<Tag> tags;
    @Builder
    @Getter
    public static class ChapterGroup{
        private String group; // chapter table의 subject에서 컬럼명 변경
        private List<Chapters> chapters;

    }
    @Builder
    @Getter
    public static class Chapters{
        private String id; //03 or 02
        private String name;  //수열 or 삼각함수 등 // chapter table chapter에서 컬럼명 변경
    }
}
