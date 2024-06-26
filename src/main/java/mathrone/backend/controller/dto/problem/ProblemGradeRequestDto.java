package mathrone.backend.controller.dto.problem;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemGradeRequestDto {

    private List<problemSolve> answerSubmitList;

    @Getter
    public static class problemSolve {

        private String problemId;
        private String myAnswer;
    }
}
