package mathrone.backend.controller;

import io.swagger.annotations.ApiOperation;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.ProblemDto;
import mathrone.backend.service.ProblemServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/problem")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemServiceImpl problemServiceImpl;

    @GetMapping("/detail-page")
    @ApiOperation(value = "문제 상세 페이지의 문제 조회")
    public ResponseEntity<ProblemDto> problemInquiry(@RequestParam(value = "problemId") String problemId){
        return ResponseEntity.ok(problemServiceImpl.findProblembyId(problemId));
    }

    @GetMapping("/detail-page/all")
    @ApiOperation(value = "문제 상세 페이지의 모든 문제 조회")
    public ResponseEntity<Set<ProblemDto>> problemList(@RequestParam(value="workbookId") String workbookId,
        @RequestParam(value="chapterId") String chapterId){
        return ResponseEntity.ok(problemServiceImpl.findProblem(workbookId,chapterId));
    }

    @GetMapping(value = {"/try", "/try/{correct}"})
    @ApiOperation(value = "유저가 시도한 문제 반환", notes = "프리미엄 유저가 푼 문제에 대한 분석 그래프 제공 기능을 위함\n"
        + " correct의 여부에 따라 유저가 시도한 문제의 정답여부에 따른 문제 반환")
    public ResponseEntity<Set<ProblemDto>> getTryProblem(HttpServletRequest request,
        @PathVariable(required = false) Optional<Boolean> correct) {
        return ResponseEntity.ok(problemServiceImpl.getTryProblem(request, correct));
    }

}
