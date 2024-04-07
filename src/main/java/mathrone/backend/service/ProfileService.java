package mathrone.backend.service;

import static mathrone.backend.error.exception.ErrorCode.EMPTY_FAILED_PROBLEM;
import static mathrone.backend.error.exception.ErrorCode.EMPTY_FAILED_PROBLEM_IN_REDIS;
import static mathrone.backend.error.exception.ErrorCode.NONEXISTENT_FAILED_CHAPTER;
import static mathrone.backend.error.exception.ErrorCode.NONEXISTENT_FAILED_WORKBOOK;
import static mathrone.backend.error.exception.ErrorCode.NOT_FOUND_CHAPTER;
import static mathrone.backend.error.exception.ErrorCode.NOT_FOUND_WORKBOOK;
import static mathrone.backend.error.exception.ErrorCode.NOT_PREMIUM;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import mathrone.backend.controller.dto.ChangeProfileDto;
import mathrone.backend.controller.dto.UserFailedTriedProblemsOfChapterDto;
import mathrone.backend.controller.dto.UserFailedTriedWorkbookResponseDto;
import mathrone.backend.controller.dto.UserFailedTriedWorkbookResponseDto.UserFailedTriedChapterDto;
import mathrone.backend.controller.dto.UserFailedTriedWorkbookResponseDto.UserFailedTriedWorkbookDto;
import mathrone.backend.domain.ChapterInfo;
import mathrone.backend.domain.ProblemTry;
import mathrone.backend.domain.Subscription;
import mathrone.backend.domain.SubscriptionInfo;
import mathrone.backend.domain.UserFailedTriedWorkbookRedis;
import mathrone.backend.domain.UserFailedTriedWorkbookRedis.UserFailedTriedChapterR;
import mathrone.backend.domain.UserFailedTriedWorkbookRedis.UserFailedTriedWorkbookR;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.domain.UserProfile;
import mathrone.backend.domain.UserRank;
import mathrone.backend.domain.WorkBookInfo;
import mathrone.backend.error.exception.CustomException;
import mathrone.backend.error.exception.ErrorCode;
import mathrone.backend.repository.ChapterRepository;
import mathrone.backend.repository.ProblemTryRepository;
import mathrone.backend.repository.SubscriptionRepository;
import mathrone.backend.repository.UserInfoRepository;
import mathrone.backend.repository.WorkBookRepository;
import mathrone.backend.repository.redisRepository.UserFailedTriedWorkbookRedisRepository;
import mathrone.backend.util.S3FileUploader;
import mathrone.backend.util.TokenProviderUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfileService {

    private final UserInfoRepository userInfoRepository;
    private final ZSetOperations<String, String> zSetOperations;
    private final TokenProviderUtil tokenProviderUtil;
    private final ProblemTryRepository problemTryRepository;
    private final WorkBookRepository workBookRepository;
    private final ChapterRepository chapterRepository;
    private final UserFailedTriedWorkbookRedisRepository userFailedTriedWorkbookRedisRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final S3FileUploader s3FileUploader;


    public ProfileService(UserInfoRepository userInfoRepository,
            RedisTemplate<String, String> redisTemplate,
            TokenProviderUtil tokenProviderUtil,
            ProblemTryRepository problemTryRepository, WorkBookRepository workBookRepository,
            ChapterRepository chapterRepository,
            UserFailedTriedWorkbookRedisRepository userFailedTriedWorkbookRedisRepository,
            SubscriptionRepository subscriptionRepository, S3FileUploader s3FileUploader) {
        this.userInfoRepository = userInfoRepository;
        this.zSetOperations = redisTemplate.opsForZSet();
        this.tokenProviderUtil = tokenProviderUtil;
        this.problemTryRepository = problemTryRepository;
        this.workBookRepository = workBookRepository;
        this.chapterRepository = chapterRepository;
        this.userFailedTriedWorkbookRedisRepository = userFailedTriedWorkbookRedisRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.s3FileUploader = s3FileUploader;
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

            if(node == null){
                r.setRank("null");
                r.setScore("null");
                r.setTrycnt("null");
            }else{
                r.setRank(node.findValue("rank").toString());
                r.setScore(node.findValue("score").toString());
                r.setTrycnt(node.findValue("try").toString());
            }

        }

        //구독정보 초기값 null
        SubscriptionInfo s;

        //구독회원인 경우
        if (userinfo.isPremium()) {
            Subscription sub = subscriptionRepository.checkLastSubscription(userinfo.getUserId())
                    .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIBE_USER_NOT_FOUND));

            Date subscribedDate = sub.getLastModifiedDate();

            LocalDate localDate = subscribedDate.toInstant()   // Date -> Instant
                    .atZone(ZoneId.systemDefault())  // Instant -> ZonedDateTime
                    .toLocalDate();

            LocalDate expiredDate = localDate.plusMonths(1);

            Timestamp expire = Timestamp.valueOf(expiredDate.atStartOfDay());
            Timestamp subscribe = new Timestamp(subscribedDate.getTime());

            s = SubscriptionInfo.builder()
                    .expired_data(expire)
                    .subscribed_date(subscribe)
                    .item(sub.getItem())
                    .price(sub.getPrice())
                    .build();
        } else {
            s = null;
        }

        //최종 Profile 생성
        return new UserProfile(userinfo.getUserId(), userinfo.getNickname(),
                userinfo.getPassword(), userinfo.getProfileImg(), userinfo.getExp(),
                userinfo.getEmail(), userinfo.getPhoneNum(),
                r, userinfo.isPremium(), s);
    }


    public ObjectNode getMyRank(Integer user_id) { // 리더보드에 필요한 나의 rank 조회
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();

        Long rank = zSetOperations.reverseRank("test", user_id.toString());
        if(rank == null){

            return null;

        }else{
            node.put("rank", zSetOperations.reverseRank("test", user_id.toString()) + 1);
            node.put("score", zSetOperations.score("test", user_id.toString()));
            node.put("try", userInfoRepository.getTryByUserID(user_id));
            return node;
        }
    }

    public UserFailedTriedWorkbookResponseDto getTriedProblemForGraph(HttpServletRequest request) {
        // 1. Request Header 에서 access token 빼기
        String accessToken = tokenProviderUtil.resolveToken(request);

        // 2. access token으로부터 user id 가져오기 (email x)
        int userId = Integer.parseInt(
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
            String[] problemInfo = userFailedTriedProblem.getProblem().getProblemId()
                    .split("-");
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
        UserFailedTriedWorkbookResponseDto userFailedTriedWorkbookResponseDto = new UserFailedTriedWorkbookResponseDto();
        Map<String, UserFailedTriedWorkbookR> userFailedTriedWorkbookListForRedis = new HashMap<>();

        // 6. 분류된 problem들을 responseDTO와 UserFailedTriedWorkbookRedis에 적용
        for (String workbookId : userFailedTriedWorkbooks.keySet()) {

            WorkBookInfo workbook = workBookRepository.findById(workbookId).
                    orElseThrow(() -> new CustomException(NOT_FOUND_WORKBOOK));

            Map<String, Integer> userFailedTriedChapters = userFailedTriedWorkbooks.get(
                    workbookId);
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
                        new UserFailedTriedChapterR(chapter.getName(),
                                userFailedTriedChaptersForRedis.get(chapterId)));
            }

            userFailedTriedWorkbookResponseDto.getFailedTriedWorkbookList().add(
                    new UserFailedTriedWorkbookDto(workbookId, workbook.getTitle(),
                            triedChapterList));

            userFailedTriedWorkbookListForRedis.put(workbookId,
                    new UserFailedTriedWorkbookR(workbook.getTitle(),
                            userFailedTriedChapterRList));
        }

        // 6. Redis에 userFailedTriedWorkbookRedis 객체 저장
        userFailedTriedWorkbookRedisRepository.save(
                UserFailedTriedWorkbookRedis.builder()
                        .userId(userId)
                        // 10분
                        .expiration(600L)
                        .userFailedTriedWorkbookList(userFailedTriedWorkbookListForRedis)
                        .build()
        );

        return userFailedTriedWorkbookResponseDto;

    }

    public UserFailedTriedProblemsOfChapterDto getUserFailedProblemsOfChapterOfWorkbook(
            HttpServletRequest request, String workbookId, String chapterId) {
        // 1. Request Header 에서 access token 빼기
        String accessToken = tokenProviderUtil.resolveToken(request);

        // 2. access token으로부터 user id 가져오기 (email x)
        int userId = Integer.parseInt(
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
                        userFailedTriedWorkbook.getUserFailedTriedWorkbookList().get(workbookId))
                .orElseThrow(
                        () -> new CustomException(NONEXISTENT_FAILED_WORKBOOK));

        // 6. 유저가 틀린 문제 중 특정 챕터의 데이터 존재 여부 체크
        UserFailedTriedChapterR userFailedTriedChapterR = Optional.ofNullable(
                        userFailedTriedWorkbookR.getUserFailedTriedChapterList().get(chapterId))
                .orElseThrow(
                        () -> new CustomException(NONEXISTENT_FAILED_CHAPTER));

        return UserFailedTriedProblemsOfChapterDto.builder()
                .workbookTitle(userFailedTriedWorkbookR.getWorkbookTitle())
                .chapterTitle(userFailedTriedChapterR.getChapterTitle())
                .problems(userFailedTriedChapterR.getTriedProblem())
                .build();
    }

    @Transactional
    public ResponseEntity changeProfile(ChangeProfileDto changeProfileDto,
            HttpServletRequest request){
        String accessToken = tokenProviderUtil.resolveToken(request);
        int userId = Integer.parseInt(
                tokenProviderUtil.getAuthentication(accessToken).getName());
        UserInfo userInfo = userInfoRepository.findByUserId(userId);
        if (!changeProfileDto.getPhoneNum().isEmpty()) {
            userInfo.setPhoneNum(changeProfileDto.getPhoneNum());
        }
        if (!changeProfileDto.getNickname().isEmpty()) {
            userInfo.setNickname(changeProfileDto.getNickname());
        }
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity changeImg(MultipartFile profileImg, HttpServletRequest request)
            throws IOException {
        String accessToken = tokenProviderUtil.resolveToken(request);
        int userId = Integer.parseInt(
                tokenProviderUtil.getAuthentication(accessToken).getName());
        UserInfo userInfo = userInfoRepository.findByUserId(userId);
        userInfo.setProfileImg(s3FileUploader.upload(profileImg));
        return ResponseEntity.ok().build();
    }
}
