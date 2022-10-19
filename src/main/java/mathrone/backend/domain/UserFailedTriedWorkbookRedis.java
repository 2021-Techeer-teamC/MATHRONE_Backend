package mathrone.backend.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import mathrone.backend.config.CacheKey;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@AllArgsConstructor
@Builder
@RedisHash(CacheKey.TriedWorkBook)
public class UserFailedTriedWorkbookRedis {
    @Id
    Integer userId;

    @TimeToLive
    private Long expiration;

    List<UserFailedTriedWorkbookR> userFailedTriedWorkbookList;

    @Getter
    @AllArgsConstructor
    public static class UserFailedTriedWorkbookR {
        private String workbookId;

        private String workbookTitle;

        List<UserFailedTriedChapterR> userFailedTriedChapterList;

    }

    @Getter
    @AllArgsConstructor
    public static class UserFailedTriedChapterR {

        private String chapterId;
        private String chapterTitle;

        List<String> triedProblem;

    }
}
