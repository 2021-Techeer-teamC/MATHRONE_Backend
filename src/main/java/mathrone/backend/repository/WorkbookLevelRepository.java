package mathrone.backend.repository;

import java.util.Optional;
import mathrone.backend.domain.WorkbookLevelInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkbookLevelRepository extends JpaRepository<WorkbookLevelInfo, Long> {
    Optional<WorkbookLevelInfo> findByWorkbookId(String workbookId);
}
