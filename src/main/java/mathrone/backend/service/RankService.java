package mathrone.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import mathrone.backend.error.exception.CustomException;
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
    public ArrayNode getAllRank() { // 리더보드에 필요한 rank 데이터 조회
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        Set<ZSetOperations.TypedTuple<String>> rankSet =
            Optional.ofNullable(zSetOperations.reverseRangeWithScores("test", 0, -1))
                .orElse(new HashSet<>());

        //LinkedHashMap으로 리턴함
        for (ZSetOperations.TypedTuple<String> str : rankSet) {
            ObjectNode node = mapper.createObjectNode();
            int temp = Integer.parseInt(str.getValue());
            node.put("user_name", userInfoRepository.findByUserId(temp).getAccountId());
            node.put("correct_count", str.getScore());
            node.put("try_count", userInfoRepository.getTryByUserID(temp));
            arrayNode.add(node);
        } // 해당 유저가 시도한 문제 수를 포함한 JSON 형식 다시 생성
        return arrayNode;
    }

    /**
     * 특정 사용자의 rank 데이터 반환
     *
     * @param request access token 정보를 추출하기 위한 매개변수
     * @return ObjectNode
     */
    public ObjectNode getMyRank(HttpServletRequest request) {
        String accessToken = tokenProviderUtil.resolveToken(request);

        if (!tokenProviderUtil.validateToken(accessToken, request)) {
            throw (CustomException) request.getAttribute("Exception");
        }

        // 리더보드에 필요한 나의 rank 조회
        int userId = Integer.parseInt(
            tokenProviderUtil.getAuthentication(accessToken).getName());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        Optional<Long> test = Optional.ofNullable(
            zSetOperations.reverseRank("test", Integer.toString(userId)));

        // redis에 data가 존재하는 경우
        if (test.isPresent()) {
            node.put("rank", test.get() + 1);
            node.put("user_name", userInfoRepository.findByUserId(userId).getAccountId());
            node.put("correct_count", zSetOperations.score("test", Integer.toString(userId)));
            node.put("try_count", userInfoRepository.getTryByUserID(userId));
        }
        return node;
    }

    public void setRank(Integer userId, Integer upScore) { // 문제를 풀었을 시에 스코어를 올려주는 용도
        zSetOperations.incrementScore("test", userId.toString(), upScore);
        // value값에 해당하는 score에 delta값을 더해줌, value 값이 없을시 자동 추가
    }
}
