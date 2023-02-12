package mathrone.backend.controller.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserFailedTriedProblemsOfChapterDto {

    String workbookTitle;

    String chapterTitle;

    List<String> problems;

}
