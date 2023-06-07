package mathrone.backend.repository;

import mathrone.backend.domain.UserWorkbookRelInfo;
import mathrone.backend.domain.WorkbookRelPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserWorkbookRelRepository extends
    JpaRepository<UserWorkbookRelInfo, WorkbookRelPK> {

}
