package mathrone.backend.service;

import java.util.Set;
import mathrone.backend.controller.dto.ProblemDto;

interface ProblemService {

    ProblemDto findProblembyId(String problemId);

    Set<ProblemDto> findProblem(String workbookId, String chapterId);

}
