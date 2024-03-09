package mathrone.backend.repository;

import java.util.List;
import java.util.Set;
import mathrone.backend.domain.ChapterInfo;
import mathrone.backend.domain.Problem;
import mathrone.backend.domain.WorkBookInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, String> {

    List<Problem> findByWorkbookAndChapterOrderByProblemId(WorkBookInfo workBook,
        ChapterInfo chapter);

    List<Problem> findByWorkbookOrderByProblemId(WorkBookInfo workBookInfo);

    @Query(value = "SELECT p.* "
        + "FROM problem as p, "
        + "(SELECT problem_id AS problemId, COUNT(problem_id) AS problemCount "
        + "FROM problem_try "
        + "GROUP BY problemId "
        + "ORDER BY problemCount DESC "
        + "LIMIT 5) pt "
        + "WHERE pt.problemId = p.problem_id "
        + "ORDER BY pt.problemCount DESC", nativeQuery = true)
    Set<Problem> findAllByProblemByUserTried();

    @Query(value = "SELECT p.* "
        + "FROM problem as p, "
        + "(SELECT problem_id AS problemId, COUNT(problem_id) AS problemCount "
        + "FROM problem_try "
        + "WHERE iscorrect = :correct "
        + "GROUP BY problemId "
        + "ORDER BY problemCount DESC "
        + "LIMIT 5) pt "
        + "WHERE pt.problemId = p.problem_id "
        + "ORDER BY pt.problemCount DESC", nativeQuery = true)
    Set<Problem> findAllByProblemByUserTriedCorrect(boolean correct);

}
