package mathrone.backend.service;

import java.util.List;
import mathrone.backend.domain.Problem;

interface ProblemService {

    public Problem findProblembyId(String problemId);

    public List<Problem> findProblem(String workbookId, String chapterId);

}
