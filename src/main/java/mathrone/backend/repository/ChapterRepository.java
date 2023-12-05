package mathrone.backend.repository;

import java.util.Optional;
import java.util.Set;
import mathrone.backend.domain.ChapterInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChapterRepository extends JpaRepository<ChapterInfo, String> {

    Optional<ChapterInfo> findByChapterId(String chapterId);

    @Query(value = "SELECT C.* FROM CHAPTER AS C WHERE C.GROUP = :group", nativeQuery = true)
    Set<ChapterInfo> findByGroup(String group);


}
