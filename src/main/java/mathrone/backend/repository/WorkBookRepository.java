package mathrone.backend.repository;

import java.util.List;
import mathrone.backend.domain.PubCatPair;
import mathrone.backend.domain.WorkBookInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkBookRepository extends JpaRepository<WorkBookInfo, String> {

    // 전체 문제집
    Page<WorkBookInfo> findAll(Pageable pageable);

    //publisher를 이용하여 workbook을 찾는 기능 -> category가 관계(all) 없는 경우
    Page<WorkBookInfo> findAllByPublisher(String publisher, Pageable pageable);

    //publisher와 category를 이용하여 workbook을 찾는 기능 -> Workbook Info 타입의 리스트를 반환
    Page<WorkBookInfo> findAllByPublisherAndCategory(String publisher, String category,
        Pageable pageable);

    //결과의 수 반환
    Long countByPublisher(String publisher);

    Long countByPublisherAndCategory(String publisher, String category);

    @Query(value = "SELECT publisher, category FROM workbook GROUP BY publisher, category", nativeQuery = true)
    List<PubCatPair> findGroupByPublisherAndCategory();

    // workbookId로 해당 workbook 조회
    WorkBookInfo findByWorkbookId(WorkBookInfo workBookInfo);

    // 모든 유저가 즐겨찾는 문제집 중, 많이 즐겨찾는 순으로 6개 조회
    @Query(value = "SELECT workbook.*"
        + "FROM (SELECT workbook_id, count(workbook_id) AS star_count "
        + "FROM user_workbook_rel "
        + "WHERE workbook_star = TRUE "
        + "GROUP BY workbook_id "
        + "ORDER BY star_count DESC , workbook_id) AS relation "
        + "INNER JOIN workbook "
        + "ON workbook.workbook_id = relation.workbook_id LIMIT 6;", nativeQuery = true)
    List<WorkBookInfo> findAllUserStarWorkBook();

    // 특정 유저가 즐겨찾는 문제집 조회
    @Query(value = "SELECT workbook.* "
        + "FROM (SELECT workbook_id "
        + "FROM user_workbook_rel "
        + "WHERE workbook_star = TRUE "
        + "AND user_id = :userId "
        + "GROUP BY workbook_id "
        + "ORDER BY workbook_id) AS relation "
        + "INNER JOIN workbook "
        + "ON workbook.workbook_id = relation.workbook_id;", nativeQuery = true)
    List<WorkBookInfo> findUserStarWorkBook(int userId);

    // 모든 유저가 시도한 문제집 중, 많이 시도한 순으로 6개 조회
    @Query(value = "SELECT workbook.*"
        + "FROM (SELECT workbook_id, count(workbook_id) AS star_count "
        + "FROM user_workbook_rel "
        + "WHERE workbook_try = TRUE "
        + "GROUP BY workbook_id "
        + "ORDER BY star_count DESC , workbook_id) AS relation "
        + "INNER JOIN workbook "
        + "ON workbook.workbook_id = relation.workbook_id LIMIT 6;", nativeQuery = true)
    List<WorkBookInfo> findAllUserTriedWorkbook();

    // 특정 유저가 시도한 문제집 조회
    @Query(value = "SELECT workbook.* "
        + "FROM (SELECT workbook_id "
        + "FROM user_workbook_rel "
        + "WHERE workbook_try = TRUE "
        + "AND user_id = :userId "
        + "GROUP BY workbook_id "
        + "ORDER BY workbook_id) AS relation "
        + "INNER JOIN workbook "
        + "ON workbook.workbook_id = relation.workbook_id;", nativeQuery = true)
    List<WorkBookInfo> findUserTriedWorkbook(int userId);


}
