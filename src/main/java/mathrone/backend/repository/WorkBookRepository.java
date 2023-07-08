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

}
