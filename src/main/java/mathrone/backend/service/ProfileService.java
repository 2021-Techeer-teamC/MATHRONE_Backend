package mathrone.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mathrone.backend.controller.dto.UserProblemTryDTO;
import mathrone.backend.controller.dto.UserFailedTriedWorkbookResponseDto;
import mathrone.backend.controller.dto.UserFailedTriedWorkbookResponseDto.UserFailedTriedChapterDto;
import mathrone.backend.controller.dto.UserFailedTriedWorkbookResponseDto.UserFailedTriedWorkbookDto;
import mathrone.backend.domain.ChapterInfo;
import mathrone.backend.domain.ProblemTry;
import mathrone.backend.domain.UserFailedTriedWorkbookRedis;
import mathrone.backend.domain.UserFailedTriedWorkbookRedis.UserFailedTriedChapterR;
import mathrone.backend.domain.UserFailedTriedWorkbookRedis.UserFailedTriedWorkbookR;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.domain.UserProfile;
import mathrone.backend.domain.UserRank;
import mathrone.backend.domain.WorkBookInfo;
import mathrone.backend.repository.ChapterRepository;
import mathrone.backend.repository.ProblemRepository;
import mathrone.backend.repository.ProblemTryRepository;
import mathrone.backend.repository.UserFailedTriedWorkbookRedisRepository;
import mathrone.backend.repository.UserInfoRepository;
import mathrone.backend.repository.WorkBookRepository;
import mathrone.backend.util.TokenProviderUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final UserInfoRepository userInfoRepository;
    private final ZSetOperations<String, String> zSetOperations;
    private final TokenProviderUtil tokenProviderUtil;
    private final ProblemRepository problemRepository;
    private final ProblemTryRepository problemTryRepository;
    private final WorkBookRepository workBookRepository;
    private final ChapterRepository chapterRepository;


    public ProfileService(UserInfoRepository userInfoRepository,
        RedisTemplate<String, String> redisTemplate,
        TokenProviderUtil tokenProviderUtil,
        ProblemRepository problemRepository,
        ProblemTryRepository problemTryRepository, WorkBookRepository workBookRepository,
        ChapterRepository chapterRepository) {
        this.userInfoRepository = userInfoRepository;
        this.zSetOperations = redisTemplate.opsForZSet();
        this.tokenProviderUtil = tokenProviderUtil;
        this.problemRepository = problemRepository;
        this.problemTryRepository = problemTryRepository;
        this.workBookRepository = workBookRepository;
        this.chapterRepository = chapterRepository;
    }

    //userId를 받아와서 전송
    public UserProfile getProfile(
        String userId) {//이 부분 수정이 필요! (현재 userId가 pk가 아닌 id로 되어있어서 임시로 이렇게 찾는 방법으로 해둠)

        //유저 정보 받아오기
        UserInfo userinfo = userInfoRepository.findByUserId(Integer.parseInt(userId));
        //랭크 정보 받아오기
        ObjectNode node = getMyRank(userinfo.getUserId());

        //랭크 정보를 DTO에 담기
        UserRank r = new UserRank(node.findValue("rank").toString(),
            node.findValue("score").toString(), node.findValue("try").toString());

        //최종 Profile 생성
        UserProfile res = new UserProfile(userinfo.getUserId(), userinfo.getId(),
            userinfo.getPassword(), userinfo.getProfileImg(), userinfo.getExp(),
            userinfo.isPremium(), userinfo.getEmail(), userinfo.getPhoneNum(),
            userinfo.getUserImg(), userinfo.getRole(), r);

        return res;
    }


    public ObjectNode getMyRank(Integer user_id) { // 리더보드에 필요한 나의 rank 조회
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("rank", zSetOperations.reverseRank("test", user_id.toString()) + 1);
        node.put("score", zSetOperations.score("test", user_id.toString()));
        node.put("try", userInfoRepository.getTryByUserID(user_id));
        return node;
    }


    public UserInfo getUserInfo(long userId) {
        return userInfoRepository.getById(userId);
    }

    public List<UserProblemTryDTO> getTryProblem(String accessToken) {
        if (!tokenProviderUtil.validateToken(accessToken)) {
            throw new RuntimeException("Access Token 이 유효하지 않습니다.");
        }
        // 2. access token으로부터 user id 가져오기 (email x)
        String userId = tokenProviderUtil.getAuthentication(accessToken).getName();

        return problemRepository.findUserTryProblem(
            Integer.parseInt(userId));
    }


    public UserFailedTriedWorkbookResponseDto getTriedProblemForGraph(String accessToken) {
        if (!tokenProviderUtil.validateToken(accessToken)) {
            throw new RuntimeException("Access Token 이 유효하지 않습니다.");
        }
        // 1. access token으로부터 user id 가져오기 (email x)
        Integer userId = Integer.parseInt(
            tokenProviderUtil.getAuthentication(accessToken).getName());

        UserInfo user = userInfoRepository.findByUserId(userId);

        // 2. check premium
        if (!user.isPremium()) {
            throw new RuntimeException("유저의 등급이 premium이 아닙니다.");
        }

        Optional<List<ProblemTry>> userFailedTriedProblemExisted = problemTryRepository.findProblemTryByUserAndIscorrect(
            user, false);

        // 3. user가 푼 문제 중, 틀린 문제가 존재하는지 여부 체크
        if (userFailedTriedProblemExisted.isEmpty()) {
            throw new RuntimeException("유저가 틀린 문제에 대한 데이터가 존재하지 않습니다");
        }
        List<ProblemTry> userFailedTriedProblemList = userFailedTriedProblemExisted.get();

        Map<String, Map<String, Integer>> userFailedTriedWorkbooks = new HashMap<>();

        // 4. 유저가 시도한 문제를 문제집에 맞게 분류
        for (ProblemTry userFailedTriedProblem : userFailedTriedProblemList) {
            String[] problemInfo = userFailedTriedProblem.getProblem().getProblemId().split("-");
            String workbookNum = problemInfo[0];
            String chapterNum = problemInfo[1];

            if (!userFailedTriedWorkbooks.containsKey(workbookNum)) {
                Map<String, Integer> userFailedTriedChapters = new HashMap<>();

                // graph를 위한 workbook data 생성
                userFailedTriedChapters.put(chapterNum, 1);
                userFailedTriedWorkbooks.put(workbookNum, userFailedTriedChapters);

            } else {
                Map<String, Integer> userFailedTriedChapters = userFailedTriedWorkbooks.get(workbookNum);

                if (!userFailedTriedChapters.containsKey(chapterNum)) {
                    // graph를 위한 chapter data 생성
                    userFailedTriedChapters.put(chapterNum, 1);
                } else {
                    // graph를 위한 problem count update
                    userFailedTriedChapters.put(chapterNum,userFailedTriedChapters.get(chapterNum)+1);
                }
            }

        }

        UserFailedTriedWorkbookResponseDto userTriedProblemForGraphResponseDto = new UserFailedTriedWorkbookResponseDto();

        for (String it : userFailedTriedWorkbooks.keySet()) {
            WorkBookInfo workbook = workBookRepository.findByWorkbookId(it);
            Map<String, Integer> userFailedTriedChapters = userFailedTriedWorkbooks.get(it);

            List<UserFailedTriedChapterDto> triedChapterList = new LinkedList<>();

            for (String chapterId : userFailedTriedChapters.keySet()){
                triedChapterList.add(new UserFailedTriedChapterDto(chapterId, userFailedTriedChapters.get(chapterId)));

            }

            userTriedProblemForGraphResponseDto.getFailedTriedWorkbookList().add(
                new UserFailedTriedWorkbookDto(workbook.getTitle(), triedChapterList));


        }

        return userTriedProblemForGraphResponseDto;
    }

}
