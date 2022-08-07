package mathrone.backend.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.ProblemGradeRequestDto;
import mathrone.backend.controller.dto.ProblemGradeResponseDto;
import mathrone.backend.service.AnswerServiceImpl;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/answer")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerServiceImpl answerService;

    @PutMapping("/problem")
    public List<ProblemGradeResponseDto> problemGrade(
        @RequestBody ProblemGradeRequestDto problemGradeRequestDtoList) {
        return answerService.gradeProblem(problemGradeRequestDtoList);
    }

}
