package mathrone.backend.service;

import static mathrone.backend.error.exception.ErrorCode.EMPTY_FAILED_PROBLEM;
import static mathrone.backend.error.exception.ErrorCode.EMPTY_FAILED_PROBLEM_IN_REDIS;
import static mathrone.backend.error.exception.ErrorCode.INVALID_ACCESS_TOKEN;
import static mathrone.backend.error.exception.ErrorCode.NONEXISTENT_FAILED_CHAPTER;
import static mathrone.backend.error.exception.ErrorCode.NONEXISTENT_FAILED_WORKBOOK;
import static mathrone.backend.error.exception.ErrorCode.NOT_FOUND_CHAPTER;
import static mathrone.backend.error.exception.ErrorCode.NOT_PREMIUM;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import mathrone.backend.controller.dto.UserFailedTriedProblemsOfChapterDto;
import mathrone.backend.controller.dto.UserProblemTryDto;
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
import mathrone.backend.error.exception.CustomException;
import mathrone.backend.repository.ChapterRepository;
import mathrone.backend.repository.ProblemRepository;
import mathrone.backend.repository.ProblemTryRepository;
import mathrone.backend.repository.redisRepository.UserFailedTriedWorkbookRedisRepository;
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
    private final UserFailedTriedWorkbookRedisRepository userFailedTriedWorkbookRedisRepository;


    public ProfileService(UserInfoRepository userInfoRepository,
        RedisTemplate<String, String> redisTemplate,
        TokenProviderUtil tokenProviderUtil,
        ProblemRepository problemRepository,
        ProblemTryRepository problemTryRepository, WorkBookRepository workBookRepository,
        ChapterRepository chapterRepository,
        UserFailedTriedWorkbookRedisRepository userFailedTriedWorkbookRedisRepository) {
        this.userInfoRepository = userInfoRepository;
        this.zSetOperations = redisTemplate.opsForZSet();
        this.tokenProviderUtil = tokenProviderUtil;
        this.problemRepository = problemRepository;
        this.problemTryRepository = problemTryRepository;
        this.workBookRepository = workBookRepository;
        this.chapterRepository = chapterRepository;
        this.userFailedTriedWorkbookRedisRepository = userFailedTriedWorkbookRedisRepository;
    }

    //userId를 받아와서 전송
    public UserProfile getProfile(
        String userId) {//이 부분 수정이 필요! (현재 userId가 pk가 아닌 id로 되어있어서 임시로 이렇게 찾는 방법으로 해둠)

        //유저 정보 받아오기
        UserInfo userinfo = userInfoRepository.findByUserId(Integer.parseInt(userId));

        int user_exp = userinfo.getExp();

        //rank 초기값 null
        UserRank r = new UserRank(null, null, null);

        if (user_exp > 0) { //exp가 0 이상인 경우에만 rank존재
            //랭크 정보 받아오기
            ObjectNode node = getMyRank(userinfo.getUserId());

            r.setRank(node.findValue("rank").toString());
            r.setScore(node.findValue("score").toString());
            r.setTrycnt(node.findValue("try").toString());

        }

        //최종 Profile 생성
        UserProfile res = new UserProfile(userinfo.getUserId(), userinfo.getAccountId(),
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

    public List<UserProblemTryDto> getTryProblem(HttpServletRequest request) {
        // 1. Request Header 에서 access token 빼기
        String accessToken = tokenProviderUtil.resolveToken(request);

        if (!tokenProviderUtil.validateToken(accessToken)) {
            throw new RuntimeException("Access Token 이 유효하지 않습니다.");
        }
        // 2. access token으로부터 user id 가져오기 (email x)
        String userId = tokenProviderUtil.getAuthentication(accessToken).getName();

        return problemRepository.findUserTryProblem(
            Integer.parseInt(userId));
    }


    public UserFailedTriedWorkbookResponseDto getTriedProblemForGraph(HttpServletRequest request) {
        // 1. Request Header 에서 access token 빼기
        String accessToken = tokenProviderUtil.resolveToken(request);

        if (!tokenProviderUtil.validateToken(accessToken)) {
            throw new CustomException(INVALID_ACCESS_TOKEN);
        }
        // 2. access token으로부터 user id 가져오기 (email x)
        Integer userId = Integer.parseInt(
            tokenProviderUtil.getAuthentication(accessToken).getName());

        UserInfo user = userInfoRepository.findByUserId(userId);

        // 3. check premium
        if (!user.isPremium()) {
            throw new CustomException(NOT_PREMIUM);
        }

        // 4. user가 푼 문제 중, 틀린 문제가 존재하는지 여부 체크
        List<ProblemTry> userFailedTriedProblemList = problemTryRepository.findProblemTryByUserAndIscorrect(
            user, false).orElseThrow(() -> new CustomException(EMPTY_FAILED_PROBLEM));

        // ResonseDto와 UserFailedTriedWorkbookRedis 객체를 만들기 전 유저가 시도한 문제들을 분류하기 위한 변수 선언
        Map<String, Map<String, Integer>> userFailedTriedWorkbooks = new HashMap<>();
        Map<String, Map<String, List<String>>> userFailedTriedWorkbooksForRedis = new HashMap<>();

        // 5. 유저가 시도한 문제를 문제집에 맞게 분류
        for (ProblemTry userFailedTriedProblem : userFailedTriedProblemList) {
            String[] problemInfo = userFailedTriedProblem.getProblem().getProblemId().split("-");
            String workbookNum = problemInfo[0];
            String chapterNum = problemInfo[1];
            String problemNum = userFailedTriedProblem.getProblem().getProblemId();

            // 5-1. 해당 workbook이 분류중인 workbook list에 없는 경우
            if (!userFailedTriedWorkbooks.containsKey(workbookNum)) {
                Map<String, Integer> userFailedTriedChapters = new HashMap<>();
                Map<String, List<String>> userFailedTriedChaptersForRedis = new HashMap<>();

                // UserFailedTriedWorkbookRedis를 위한 workbook, chapter 분류
                List<String> problemList = new ArrayList<>();
                problemList.add(problemNum);
                userFailedTriedChaptersForRedis.put(chapterNum, problemList);
                userFailedTriedWorkbooksForRedis.put(workbookNum, userFailedTriedChaptersForRedis);

                // responseDTO를 위한 workbook, chapter 분류
                userFailedTriedChapters.put(chapterNum, 1);
                userFailedTriedWorkbooks.put(workbookNum, userFailedTriedChapters);

                // 5-2. 해당 workbook이 분류중인 workbook list에 존재할 때
            } else {
                Map<String, Integer> userFailedTriedChapters = userFailedTriedWorkbooks.get(
                    workbookNum);
                Map<String, List<String>> userFailedTriedChaptersForRedis = userFailedTriedWorkbooksForRedis.get(
                    workbookNum);

                // 5-2-1 해당 chapter가 분류중인 chapter list에 없는 경우
                if (!userFailedTriedChapters.containsKey(chapterNum)) {

                    // UserFailedTriedWorkbookRedis를 위한 chapter 분류
                    List<String> problemList = new ArrayList<>();
                    problemList.add(problemNum);
                    userFailedTriedChaptersForRedis.put(chapterNum, problemList);

                    // responseDTO를 위한 chapter 분류
                    userFailedTriedChapters.put(chapterNum, 1);

                    // 5-2-2 해당 chapter가 분류중인 chapter list에 존재할 때
                } else {
                    // UserFailedTriedWorkbookRedis를 위한 chapter update
                    userFailedTriedChaptersForRedis.get(chapterNum).add(problemNum);

                    // responseDTO를 위한 chapter update
                    userFailedTriedChapters.put(chapterNum,
                        userFailedTriedChapters.get(chapterNum) + 1);
                }
            }

        }

        // responseDTO 와 redis에 저장할 변수 선언
        UserFailedTriedWorkbookResponseDto userTriedProblemForGraphResponseDto = new UserFailedTriedWorkbookResponseDto();
        Map<String, UserFailedTriedWorkbookR> userFailedTriedWorkbookListForRedis = new HashMap<>();

        // 6. 분류된 problem들을 responseDTO와 UserFailedTriedWorkbookRedis에 적용
        for (String workbookId : userFailedTriedWorkbooks.keySet()) {
            WorkBookInfo workbook = workBookRepository.findByWorkbookId(workbookId);
            Map<String, Integer> userFailedTriedChapters = userFailedTriedWorkbooks.get(workbookId);
            Map<String, List<String>> userFailedTriedChaptersForRedis = userFailedTriedWorkbooksForRedis.get(
                workbookId);

            // responseDTO와 UserFailedTriedWorkbookRedis에 적용하기 위한 chapter 선언
            List<UserFailedTriedChapterDto> triedChapterList = new LinkedList<>();
            Map<String, UserFailedTriedChapterR> userFailedTriedChapterRList = new HashMap<>();

            // workbook의 chapter별 problem들을 responseDTO와 UserFailedTriedWorkbookRedis에 적용
            for (String chapterId : userFailedTriedChapters.keySet()) {
                triedChapterList.add(new UserFailedTriedChapterDto(chapterId,
                    userFailedTriedChapters.get(chapterId)));

                // chapter의 chapterTitle을 위해 chapterId에 해당하는 객체 가져옴
                ChapterInfo chapter = chapterRepository.findByChapterId(chapterId).
                    orElseThrow(() -> new CustomException(NOT_FOUND_CHAPTER));

                userFailedTriedChapterRList.put(chapterId,
                    new UserFailedTriedChapterR(chapter.getChapter(),
                        userFailedTriedChaptersForRedis.get(chapterId)));
            }

            userTriedProblemForGraphResponseDto.getFailedTriedWorkbookList().add(
                new UserFailedTriedWorkbookDto(workbook.getTitle(), triedChapterList));

            userFailedTriedWorkbookListForRedis.put(workbookId,
                new UserFailedTriedWorkbookR(workbook.getTitle(),
                    userFailedTriedChapterRList));
        }

        // 6. Redis에 userFailedTriedWorkbookRedis 객체 저장
        userFailedTriedWorkbookRedisRepository.save(
            UserFailedTriedWorkbookRedis.builder()
                .userId(userId)
                // TTL 시간은 추후에 상의하여 다시 설정하기
                .expiration(300L)
                .userFailedTriedWorkbookList(userFailedTriedWorkbookListForRedis)
                .build()
        );

        return userTriedProblemForGraphResponseDto;
    }

    public UserFailedTriedProblemsOfChapterDto getUserFailedProblemsOfChapterOfWorkbook(
        HttpServletRequest request, String workbookId, String chapterId) {
        // 1. Request Header 에서 access token 빼기
        String accessToken = tokenProviderUtil.resolveToken(request);

        if (!tokenProviderUtil.validateToken(accessToken)) {
            throw new CustomException(INVALID_ACCESS_TOKEN);
        }
        // 2. access token으로부터 user id 가져오기 (email x)
        Integer userId = Integer.parseInt(
            tokenProviderUtil.getAuthentication(accessToken).getName());

        UserInfo user = userInfoRepository.findByUserId(userId);

        // 3. check premium
        if (!user.isPremium()) {
            throw new CustomException(NOT_PREMIUM);
        }

        // 4. Redis에 유저가 틀린 문제에 대한 데이터가 존재하는지 여부 체크
        UserFailedTriedWorkbookRedis userFailedTriedWorkbook = userFailedTriedWorkbookRedisRepository.findById(
            userId).orElseThrow(() -> new CustomException(EMPTY_FAILED_PROBLEM_IN_REDIS));

        // 5. 유저가 틀린 문제 중 특정 문제집의 데이터 존재 여부 체크
        UserFailedTriedWorkbookR userFailedTriedWorkbookR = Optional.ofNullable(
            userFailedTriedWorkbook.getUserFailedTriedWorkbookList().get(workbookId)).orElseThrow(
                () -> new CustomException(NONEXISTENT_FAILED_WORKBOOK));

        // 6. 유저가 틀린 문제 중 특정 챕터의 데이터 존재 여부 체크
        UserFailedTriedChapterR userFailedTriedChapterR = Optional.ofNullable(
            userFailedTriedWorkbookR.getUserFailedTriedChapterList().get(chapterId)).orElseThrow(
                () -> new CustomException(NONEXISTENT_FAILED_CHAPTER));

        return UserFailedTriedProblemsOfChapterDto.builder()
            .workbookTitle(userFailedTriedWorkbookR.getWorkbookTitle())
            .chapterTitle(userFailedTriedChapterR.getChapterTitle())
            .problems(userFailedTriedChapterR.getTriedProblem())
            .build();
    }
}
