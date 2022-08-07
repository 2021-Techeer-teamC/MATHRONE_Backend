package mathrone.backend.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.ProblemGradeRequestDto;
import mathrone.backend.controller.dto.ProblemGradeResponseDto;
import mathrone.backend.domain.Problem;
import mathrone.backend.service.ProblemServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/problem")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemServiceImpl problemServiceImpl;

    @GetMapping("/detail-page") // 문제 상세 페이지의 문제 조회
    public Problem problemInquiry(@RequestParam(value = "problemId") String problemId){
        return problemServiceImpl.findProblembyId(problemId);
    }

    @GetMapping("/detail-page/all") // 모든 문제 조회(Books page)
    public List<Problem> problemList(@RequestParam(value="workbookId") String workbookId,
        @RequestParam(value="chapterId") String chapterId){
        return problemServiceImpl.findProblem(workbookId,chapterId);
    }
}
