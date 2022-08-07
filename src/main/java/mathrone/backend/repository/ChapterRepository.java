package mathrone.backend.repository;

import java.util.List;
import java.util.Optional;
import mathrone.backend.domain.ChapterInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChapterRepository extends JpaRepository<ChapterInfo, Long> {

    Optional<ChapterInfo> findByChapterId(String chapterId);
}
