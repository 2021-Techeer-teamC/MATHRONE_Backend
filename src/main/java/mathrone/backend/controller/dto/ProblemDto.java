package mathrone.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mathrone.backend.domain.Problem;

@Getter
@NoArgsConstructor
public class ProblemDto {

    private String problemId;
    private String problemNum;
    private String problemImg;
    private int levelOfDiff;
    private boolean multiple;
    private Workbook workbook;
    private Chapter chapter;

    public ProblemDto(Problem problem) {
        this.problemId = problem.getProblemId();
        this.problemNum = problem.getProblemNum();
        this.problemImg = problem.getProblemImg();
        this.levelOfDiff = problem.getLevelOfDiff();
        this.multiple = problem.isMultiple();
        this.workbook = new Workbook(problem.getWorkbook().getWorkbookId(), problem.getWorkbook().getTitle());
        this.chapter = new Chapter(problem.getChapter().getChapterId(), problem.getChapter().getName());
    }

    @Getter
    @AllArgsConstructor
    private static class Workbook {
        private String id;
        private String title;
    }

    @Getter
    @AllArgsConstructor
    private static class Chapter {
        private String id;
        private String title;
    }
}
