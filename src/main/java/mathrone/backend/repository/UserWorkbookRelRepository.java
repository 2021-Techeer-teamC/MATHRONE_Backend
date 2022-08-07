package mathrone.backend.repository;

import mathrone.backend.domain.UserWorkbookRelInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserWorkbookRelRepository extends JpaRepository<UserWorkbookRelInfo, Long> {
    @Query(value = "SELECT * FROM user_workbook_rel WHERE user_id=:userId AND workbook_try=TRUE", nativeQuery = true)
    List<UserWorkbookRelInfo> findByUserIdAndWorkbookTry(int userId);

    @Query(value = "SELECT * FROM user_workbook_rel WHERE user_id=:userId AND workbook_star=TRUE", nativeQuery = true)
    List<UserWorkbookRelInfo> findByUserIdAndWorkbookStar(int userId);
}
