package mathrone.backend.repository;

import mathrone.backend.domain.WorkbookRecommend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkBookRecommendRepository extends JpaRepository<WorkbookRecommend, Long> {
}
