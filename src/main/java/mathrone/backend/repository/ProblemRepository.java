package mathrone.backend.repository;

import mathrone.backend.domain.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByWorkbookIdAndChapterId(String workbookId, String chapterId);

    Problem findByProblemId(String problemId);
}
