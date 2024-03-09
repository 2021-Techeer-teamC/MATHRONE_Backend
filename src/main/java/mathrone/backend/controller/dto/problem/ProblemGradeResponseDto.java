package mathrone.backend.controller.dto.problem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProblemGradeResponseDto {

    private String problemId;
    private Integer correctAnswer;
    private Integer myAnswer;
}
