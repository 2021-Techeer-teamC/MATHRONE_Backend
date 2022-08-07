package mathrone.backend.repository;

import mathrone.backend.domain.WorkbookLevelInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkbookLevelRepository extends JpaRepository<WorkbookLevelInfo, Long> {
    WorkbookLevelInfo findByWorkbookId(String workbookId);
}
