package mathrone.backend.controller;

import io.swagger.annotations.ApiOperation;
import java.util.List;
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

    @GetMapping(value = "")
    @ApiOperation(value = "요청에 따른 문제 리스트 조회",
        notes = "problemId가 존재하면 해당 문제 조회, 아닌 경우 workbookId의 chapterId에 해당하는 모든 문제 조회")
    public ResponseEntity<List<ProblemDto>> problemList(
        @RequestParam(required = false, defaultValue = "") String workbookId,
        @RequestParam(required = false, defaultValue = "") String chapterId,
        @RequestParam(required = false, defaultValue = "") String problemId) {
        if (!problemId.isEmpty()) {
            return ResponseEntity.ok(List.of(problemServiceImpl.findProblemById(problemId)));
        } else {
            return ResponseEntity.ok(problemServiceImpl.findProblem(workbookId, chapterId));
        }
    }

    @GetMapping(value = {"/try/{onlyIncorrect}"})
    @ApiOperation(value = "유저가 시도한 문제 반환", notes = "프리미엄 유저가 푼 문제에 대한 분석 그래프 제공 기능을 위함\n"
        + " correct의 여부에 따라 유저가 시도한 문제의 정답여부에 따른 문제 반환")
    public ResponseEntity<List<ProblemDto>> getTryProblem(HttpServletRequest request,
        @PathVariable Boolean onlyIncorrect) {
        return ResponseEntity.ok(problemServiceImpl.getTryProblem(request, onlyIncorrect));
    }

}
