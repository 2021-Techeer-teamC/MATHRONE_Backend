package mathrone.backend.service;

import java.util.List;
import mathrone.backend.controller.dto.problem.ProblemDto;

interface ProblemService {

    ProblemDto findProblemById(String problemId);

    List<ProblemDto> findProblem(String workbookId, String chapterId);

}
