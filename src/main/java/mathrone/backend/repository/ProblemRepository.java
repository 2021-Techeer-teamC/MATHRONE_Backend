package mathrone.backend.repository;

import java.util.Set;
import mathrone.backend.domain.ChapterInfo;
import mathrone.backend.domain.Problem;
import mathrone.backend.domain.WorkBookInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, String> {

    Set<Problem> findByWorkbookAndChapter(WorkBookInfo workBook, ChapterInfo chapter);
}
