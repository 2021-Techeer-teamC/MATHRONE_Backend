package mathrone.backend.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import mathrone.backend.controller.dto.RankDto;
import mathrone.backend.error.exception.CustomException;
import mathrone.backend.error.exception.ErrorCode;
import mathrone.backend.repository.UserInfoRepository;
import mathrone.backend.util.TokenProviderUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;


@Service
public class RankService {

    private final ZSetOperations<String, String> zSetOperations;
    private final UserInfoRepository userInfoRepository;
    private final TokenProviderUtil tokenProviderUtil;

    public RankService(RedisTemplate<String, String> redisTemplate,
        UserInfoRepository userInfoRepository,
        TokenProviderUtil tokenProviderUtil) {
        this.zSetOperations = redisTemplate.opsForZSet();
        this.userInfoRepository = userInfoRepository;
        this.tokenProviderUtil = tokenProviderUtil;
    }

    /**
     * 모든 사용자의 rank 데이터(유저 이름, 시도, 맞은 개수)를 기반으로 rank 측정
     *
     * @return ArrayNode
     */
    public List<RankDto> getAllRank() { // 리더보드에 필요한 rank 데이터 조회
        Set<ZSetOperations.TypedTuple<String>> rankSet =
            Optional.ofNullable(zSetOperations.reverseRangeWithScores("test", 0, 9))
                .orElse(new HashSet<>());

        //LinkedHashMap으로 리턴함
        List<RankDto> result = new ArrayList<>();
        Long ranking = 0L;
        for (ZSetOperations.TypedTuple<String> str : rankSet) {
            ranking++;
            int temp = Integer.parseInt(str.getValue());
            result.add(RankDto.builder()
                        .rank(ranking)
                        .nickname(userInfoRepository.findByUserId(temp).getNickname())
                        .correct_count(Objects.requireNonNull(str.getScore()).longValue())
                        .try_count(userInfoRepository.getTryByUserID(temp))
                        .build());
        }
        return result;
    }

    /**
     * 특정 사용자의 rank 데이터 반환
     *
     * @param request access token 정보를 추출하기 위한 매개변수
     * @return ObjectNode
     */
    public RankDto getMyRank(HttpServletRequest request) {
        String accessToken = tokenProviderUtil.resolveToken(request);

        if (!tokenProviderUtil.validateToken(accessToken, request)) {
            throw (CustomException) request.getAttribute("Exception");
        }
        // 리더보드에 필요한 나의 rank 조회
        int userId = Integer.parseInt(
            tokenProviderUtil.getAuthentication(accessToken).getName());

        Optional<Long> rankList = Optional.ofNullable( // redis에서 가져온 랭킹 데이터
            zSetOperations.reverseRank("test", Integer.toString(userId)));


        if (rankList.isPresent()) { // redis에 data가 존재하는 경우
             return RankDto.builder()
                            .rank(rankList.get() + 1)
                            .nickname(userInfoRepository.findByUserId(userId).getNickname())
                            .correct_count(Objects.requireNonNull(
                                    zSetOperations.score("test", Integer.toString(userId))).longValue())
                            .try_count(userInfoRepository.getTryByUserID(userId))
                                    .build();
        }
        else{ // redis에 data가 없을 경우
            return  RankDto.builder()
                    .rank(0L)
                    .nickname(userInfoRepository.findByUserId(userId).getNickname())
                    .correct_count(0L)
                    .try_count(0L)
                    .build();
        }
    }

    @Transactional
    public void setRank(Integer userId, Integer upScore) { // 문제를 풀었을 시에 스코어를 올려주는 용도
        zSetOperations.incrementScore("test", userId.toString(), upScore);
        // value값에 해당하는 score에 delta값을 더해줌, value 값이 없을시 자동 추가
        userInfoRepository.findByUserId(userId).updateExp(upScore);
    }
}
