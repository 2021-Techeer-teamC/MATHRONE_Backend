package mathrone.backend.repository;

import mathrone.backend.domain.Problem;
import mathrone.backend.domain.UserWorkbookInfo;
import mathrone.backend.domain.UserWorkbookInfo;
import mathrone.backend.domain.WorkbookLevelInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserWorkbookRepository extends JpaRepository<UserWorkbookInfo, Long> {

    //결과의 수 반환
    Long countByWorkbookIdAndWorkbookStar(String workbookId, boolean star);

}
