package mathrone.backend.repository;

import mathrone.backend.domain.UserWorkbookRelInfo;
import mathrone.backend.domain.WorkbookRelPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserWorkbookRelRepository extends
    JpaRepository<UserWorkbookRelInfo, WorkbookRelPK> {

    // 모든 유저가 즐겨찾는 문제집 중, 중복되는 workbook_id를 제외하는 sql
    @Query(value = "SELECT w.* "
        + "FROM "
        + "(SELECT *, row_number() over (partition by workbook_id order by workbook_id) as seq "
        + "FROM user_workbook_rel "
        + "WHERE workbook_star = true) as w "
        + "WHERE w.seq = 1;", nativeQuery = true)
    List<UserWorkbookRelInfo> findAllUserStarWorkBook();

    // 특정 유저가 즐겨찾는 문제집 중, 중복되는 workbook_id를 제외하는 sql
    @Query(value = "SELECT * "
        + "FROM user_workbook_rel "
        + "WHERE workbook_star = :workbookStar "
        + "AND user_id = :userId", nativeQuery = true)
    List<UserWorkbookRelInfo> findUserStarWorkBook(int userId, boolean workbookStar);

    // 모든 유저가 시도한 문제집 중, 중복되는 workbook_id를 제외하는 sql
    @Query(value = "SELECT w.* "
        + "FROM "
        + "(SELECT *, row_number() over (partition by workbook_id order by workbook_id) as seq "
        + "FROM user_workbook_rel "
        + "WHERE workbook_try = true) as w "
        + "WHERE w.seq = 1;", nativeQuery = true)
    List<UserWorkbookRelInfo> findAllUserTriedWorkbook();

    // 특정 유저가 시도한 문제집 중, 중복되는 workbook_id를 제외하는 sql
    @Query(value = "SELECT * "
        + "FROM user_workbook_rel "
        + "WHERE workbook_try = :workbookTry "
        + "AND user_id = :userId", nativeQuery = true)
    List<UserWorkbookRelInfo> findUserTriedWorkbook(int userId, boolean workbookTry);


}
