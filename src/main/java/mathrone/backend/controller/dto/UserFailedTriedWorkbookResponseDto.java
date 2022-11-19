package mathrone.backend.controller.dto;

import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserFailedTriedWorkbookResponseDto {

    List<UserFailedTriedWorkbookDto> failedTriedWorkbookList = new LinkedList<>();

    @AllArgsConstructor
    @Getter
    public static class UserFailedTriedWorkbookDto {

        private String workbookTitle;
        List<UserFailedTriedChapterDto> chapters;
    }

    @AllArgsConstructor
    @Getter
    public static class UserFailedTriedChapterDto {
        private String id;
        private Integer failedProblemCount;
    }
}
