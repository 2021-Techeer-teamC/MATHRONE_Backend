package mathrone.backend.repository;

import java.util.List;
import java.util.Optional;
import mathrone.backend.controller.dto.UserWorkbookDataInterface;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.domain.UserWorkbookRelInfo;
import mathrone.backend.domain.WorkBookInfo;
import mathrone.backend.domain.WorkbookRelPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserWorkbookRelRepository extends
    JpaRepository<UserWorkbookRelInfo, WorkbookRelPK> {
    Optional<UserWorkbookRelInfo> findByUserAndWorkbook(UserInfo user, WorkBookInfo workBook);


    Long countByWorkbookAndWorkbookStar(WorkBookInfo workBookInfo, boolean star);

    // 모든 유저가 즐겨찾는 문제집 중, 많이 즐겨찾는 순으로 6개 조회
    @Query(value =
        "SELECT r.workbook_id as workbookId, w.title, w.thumbnail, w.publisher, false AS star, "
            + "(CASE GREATEST(l.low_cnt, l.mid_cnt, l.high_cnt) "
            + "WHEN l.low_cnt THEN '1' "
            + "WHEN l.mid_cnt THEN '2' "
            + "WHEN l.high_cnt THEN '3' END) AS level "
            + "FROM (SELECT workbook_id, count(workbook_id) as star_count "
            + "FROM user_workbook_rel "
            + "WHERE workbook_star = TRUE "
            + "GROUP BY workbook_id) AS r "
            + "INNER JOIN workbook as w "
            + "ON w.workbook_id = r.workbook_id "
            + "INNER JOIN workbook_level as l "
            + "on w.workbook_id = l.workbook_id "
            + "ORDER BY r.star_count desc, r.workbook_id LIMIT 6", nativeQuery = true)
    List<UserWorkbookDataInterface> findAllUserStarWorkBook();

    // 특정 유저가 즐겨찾는 문제집 조회
    @Query(value =
        "SELECT w.workbook_id AS workbookId, w.title, w.thumbnail, w.publisher, r.workbook_star AS star, "
            + "(CASE GREATEST(l.low_cnt, l.mid_cnt, l.high_cnt) "
            + "WHEN l.low_cnt THEN '1' "
            + "WHEN l.mid_cnt THEN '2' "
            + "WHEN l.high_cnt THEN '3' END) AS level "
            + "FROM (SELECT workbook_id, workbook_star "
            + "FROM user_workbook_rel "
            + "WHERE workbook_star = TRUE "
            + "AND user_id = :userId) AS r "
            + "INNER JOIN workbook AS w "
            + "ON w.workbook_id = r.workbook_id "
            + "INNER JOIN workbook_level AS l "
            + "ON w.workbook_id = l.workbook_id "
            + "ORDER BY w.workbook_id", nativeQuery = true)
    List<UserWorkbookDataInterface> findUserStarWorkBook(int userId);

    // 모든 유저가 시도한 문제집 중, 많이 시도한 순으로 6개 조회
    @Query(value =
        "SELECT r.workbook_id as workbookId, w.title, w.thumbnail, w.publisher, false AS star, "
            + "(CASE GREATEST(l.low_cnt, l.mid_cnt, l.high_cnt) "
            + "WHEN l.low_cnt THEN '1' "
            + "WHEN l.mid_cnt THEN '2' "
            + "WHEN l.high_cnt THEN '3' END) AS level "
            + "FROM (SELECT workbook_id, count(workbook_id) as try_count "
            + "FROM user_workbook_rel "
            + "WHERE workbook_try = TRUE "
            + "GROUP BY workbook_id) AS r "
            + "INNER JOIN workbook as w "
            + "ON w.workbook_id = r.workbook_id "
            + "INNER JOIN workbook_level as l "
            + "on w.workbook_id = l.workbook_id "
            + "ORDER BY r.try_count desc, r.workbook_id LIMIT 6", nativeQuery = true)
    List<UserWorkbookDataInterface> findAllUserTriedWorkbook();

    // 특정 유저가 시도한 문제집 조회
    @Query(value =
        "SELECT w.workbook_id AS workbookId, w.title, w.thumbnail, w.publisher, r.workbook_star AS star, "
            + "(CASE GREATEST(l.low_cnt, l.mid_cnt, l.high_cnt) "
            + "WHEN l.low_cnt THEN '1' "
            + "WHEN l.mid_cnt THEN '2' "
            + "WHEN l.high_cnt THEN '3' END) AS level "
            + "FROM (SELECT workbook_id, workbook_star "
            + "FROM user_workbook_rel "
            + "WHERE workbook_try = TRUE "
            + "AND user_id = :userId) AS r "
            + "INNER JOIN workbook AS w "
            + "ON w.workbook_id = r.workbook_id "
            + "INNER JOIN workbook_level AS l "
            + "ON w.workbook_id = l.workbook_id "
            + "ORDER BY w.workbook_id", nativeQuery = true)
    List<UserWorkbookDataInterface> findUserTriedWorkbook(int userId);

}
