package mathrone.backend.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
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
public class UserFailedTriedWorkbookRedis implements Serializable {
    @Id
    Integer userId;

    @TimeToLive
    private Long expiration;

    Map<String, UserFailedTriedWorkbookR> userFailedTriedWorkbookList;

    @Getter
    @AllArgsConstructor
    public static class UserFailedTriedWorkbookR {

        private String workbookTitle;

        Map<String, UserFailedTriedChapterR> userFailedTriedChapterList;

    }

    @Getter
    @AllArgsConstructor
    public static class UserFailedTriedChapterR {

        private String chapterTitle;

        List<String> triedProblem;

    }
}
