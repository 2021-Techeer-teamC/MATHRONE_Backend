package mathrone.backend.controller.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class ProblemGradeRequestDto {
    private int userId;
    private List<problemSolve> answerSubmitList;

    @Getter
    public static class problemSolve{
        private String problemId;
        private int solution;
    }
}
