package mathrone.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mathrone.backend.repository.UserInfoRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import mathrone.backend.util.TokenProviderUtil;


import java.util.Set;


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

    public ArrayNode getAllRank(){ // 리더보드에 필요한 rank 데이터 조회
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        Set<ZSetOperations.TypedTuple<String>> rankSet = zSetOperations.reverseRangeWithScores("test", 0, -1);
        //LinkedHashMap으로 리턴함
        for(ZSetOperations.TypedTuple<String> str : rankSet) {
            ObjectNode node = mapper.createObjectNode();
            int temp = Integer.parseInt(str.getValue());
            node.put("user_name", userInfoRepository.findByUserId(temp).getAccountId());
            node.put("correct_count", str.getScore());
            node.put("try_count", userInfoRepository.getTryByUserID(temp));
            arrayNode.add(node);
        } // 해당 유저가 시도한 문제 수를 포함한 JSON 형식 다시 생성
        return arrayNode;
    }

    public ObjectNode getMyRank(String accessToken){ // 리더보드에 필요한 나의 rank 조회
        if (!tokenProviderUtil.validateToken(accessToken)) {
            throw new RuntimeException("Access Token 이 유효하지 않습니다.");
        }

        int userId = Integer.parseInt(
                tokenProviderUtil.getAuthentication(accessToken).getName());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("rank", zSetOperations.reverseRank("test", Integer.toString(userId)) + 1);
        node.put("user_name", userInfoRepository.findByUserId(userId).getAccountId());
        node.put("correct_count", zSetOperations.score("test", Integer.toString(userId)));
        node.put("try_count", userInfoRepository.getTryByUserID(userId));
        return node;
    }

    public void setRank(Integer userId, Integer upScore){ // 문제를 풀었을 시에 스코어를 올려주는 용도
        zSetOperations.incrementScore("test", userId.toString(), upScore);
        // value값에 해당하는 score에 delta값을 더해줌, value 값이 없을시 자동 추가
    }
}
