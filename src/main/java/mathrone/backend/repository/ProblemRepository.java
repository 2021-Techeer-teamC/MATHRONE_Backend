package mathrone.backend.repository;

import java.util.List;
import mathrone.backend.domain.ChapterInfo;
import mathrone.backend.domain.Problem;
import mathrone.backend.domain.WorkBookInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, String> {

    List<Problem> findByWorkbookAndChapterOrderByProblemId(WorkBookInfo workBook,
        ChapterInfo chapter);

    List<Problem> findByWorkbookOrderByProblemId(WorkBookInfo workBookInfo);

}
