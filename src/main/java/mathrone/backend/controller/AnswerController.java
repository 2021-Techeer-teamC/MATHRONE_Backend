package mathrone.backend.controller;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.ProblemGradeRequestDto;
import mathrone.backend.controller.dto.ProblemGradeResponseDto;
import mathrone.backend.service.AnswerServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/answer")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerServiceImpl answerService;

    @PostMapping ("")
    @ApiOperation(value = "문제 체점 api", notes = "사용자가 푼 문제 리스트를 받아 채점 및 정답여부를 반환")
    public List<ProblemGradeResponseDto> problemGrade(
        @RequestBody ProblemGradeRequestDto problemGradeRequestDtoList,
        HttpServletRequest request) {
        return answerService.gradeProblem(problemGradeRequestDtoList, request);
    }

}
