package mathrone.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
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

    //userId??? ???????????? ??????
    public UserProfile getProfile(
        String userId) {//??? ?????? ????????? ??????! (?????? userId??? pk??? ?????? id??? ??????????????? ????????? ????????? ?????? ???????????? ??????)


        //?????? ?????? ????????????
        UserInfo userinfo = userInfoRepository.findByUserId(Integer.parseInt(userId));

        int user_exp = userinfo.getExp();

        //rank ????????? null
        UserRank r = new UserRank(null,null,null);

        if(user_exp > 0){ //exp??? 0 ????????? ???????????? rank??????
            //?????? ?????? ????????????
            ObjectNode node = getMyRank(userinfo.getUserId());

            r.setRank(node.findValue("rank").toString());
            r.setScore(node.findValue("score").toString());
            r.setTrycnt(node.findValue("try").toString());

        }

        //?????? Profile ??????
        UserProfile res = new UserProfile(userinfo.getUserId(), userinfo.getAccountId(),
            userinfo.getPassword(), userinfo.getProfileImg(), userinfo.getExp(),
            userinfo.isPremium(), userinfo.getEmail(), userinfo.getPhoneNum(),
            userinfo.getUserImg(), userinfo.getRole(), r);


        return res;
    }


    public ObjectNode getMyRank(Integer user_id){ // ??????????????? ????????? ?????? rank ??????
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

    public List<UserProblemTryDTO> getTryProblem(HttpServletRequest request) {
        // 1. Request Header ?????? access token ??????
        String accessToken = tokenProviderUtil.resolveToken(request);

        if (!tokenProviderUtil.validateToken(accessToken)) {
            throw new RuntimeException("Access Token ??? ???????????? ????????????.");
        }
        // 2. access token???????????? user id ???????????? (email x)
        String userId = tokenProviderUtil.getAuthentication(accessToken).getName();

        return problemRepository.findUserTryProblem(
            Integer.parseInt(userId));
    }


    public UserFailedTriedWorkbookResponseDto getTriedProblemForGraph(HttpServletRequest request) {
        // 1. Request Header ?????? access token ??????
        String accessToken = tokenProviderUtil.resolveToken(request);

        if (!tokenProviderUtil.validateToken(accessToken)) {
            throw new RuntimeException("Access Token ??? ???????????? ????????????.");
        }
        // 2. access token???????????? user id ???????????? (email x)
        Integer userId = Integer.parseInt(
            tokenProviderUtil.getAuthentication(accessToken).getName());

        UserInfo user = userInfoRepository.findByUserId(userId);

        // 3. check premium
        if (!user.isPremium()) {
            throw new RuntimeException("????????? ????????? premium??? ????????????.");
        }

        Optional<List<ProblemTry>> userFailedTriedProblemExisted = problemTryRepository.findProblemTryByUserAndIscorrect(
            user, false);

        // 4. user??? ??? ?????? ???, ?????? ????????? ??????????????? ?????? ??????
        if (userFailedTriedProblemExisted.isEmpty()) {
            throw new RuntimeException("????????? ?????? ????????? ?????? ???????????? ???????????? ????????????");
        }
        List<ProblemTry> userFailedTriedProblemList = userFailedTriedProblemExisted.get();

        // ResonseDto??? UserFailedTriedWorkbookRedis ????????? ????????? ??? ????????? ????????? ???????????? ???????????? ?????? ?????? ??????
        Map<String, Map<String, Integer>> userFailedTriedWorkbooks = new HashMap<>();
        Map<String, Map<String, List<String>>> userFailedTriedWorkbooksForRedis = new HashMap<>();

        // 5. ????????? ????????? ????????? ???????????? ?????? ??????
        for (ProblemTry userFailedTriedProblem : userFailedTriedProblemList) {
            String[] problemInfo = userFailedTriedProblem.getProblem().getProblemId().split("-");
            String workbookNum = problemInfo[0];
            String chapterNum = problemInfo[1];
            String problemNum = userFailedTriedProblem.getProblem().getProblemId();

            // 5-1. ?????? workbook??? ???????????? workbook list??? ?????? ??????
            if (!userFailedTriedWorkbooks.containsKey(workbookNum)) {
                Map<String, Integer> userFailedTriedChapters = new HashMap<String, Integer>();
                Map<String, List<String>> userFailedTriedChaptersForRedis = new HashMap<String, List<String>>();

                // UserFailedTriedWorkbookRedis??? ?????? workbook, chapter ??????
                List<String> problemList = new ArrayList<>();
                problemList.add(problemNum);
                userFailedTriedChaptersForRedis.put(chapterNum, problemList);
                userFailedTriedWorkbooksForRedis.put(workbookNum, userFailedTriedChaptersForRedis);

                // responseDTO??? ?????? workbook, chapter ??????
                userFailedTriedChapters.put(chapterNum, 1);
                userFailedTriedWorkbooks.put(workbookNum, userFailedTriedChapters);

                // 5-2. ?????? workbook??? ???????????? workbook list??? ????????? ???
            } else {
                Map<String, Integer> userFailedTriedChapters = userFailedTriedWorkbooks.get(
                    workbookNum);
                Map<String, List<String>> userFailedTriedChaptersForRedis = userFailedTriedWorkbooksForRedis.get(
                    workbookNum);

                // 5-2-1 ?????? chapter??? ???????????? chapter list??? ?????? ??????
                if (!userFailedTriedChapters.containsKey(chapterNum)) {

                    // UserFailedTriedWorkbookRedis??? ?????? chapter ??????
                    List<String> problemList = new ArrayList<String>();
                    problemList.add(problemNum);
                    userFailedTriedChaptersForRedis.put(chapterNum, problemList);

                    // responseDTO??? ?????? chapter ??????
                    userFailedTriedChapters.put(chapterNum, 1);

                    // 5-2-2 ?????? chapter??? ???????????? chapter list??? ????????? ???
                } else {
                    // UserFailedTriedWorkbookRedis??? ?????? chapter update
                    userFailedTriedChaptersForRedis.get(chapterNum).add(problemNum);

                    // responseDTO??? ?????? chapter update
                    userFailedTriedChapters.put(chapterNum,
                        userFailedTriedChapters.get(chapterNum) + 1);
                }
            }

        }

        // responseDTO ??? redis??? ????????? ?????? ??????
        UserFailedTriedWorkbookResponseDto userTriedProblemForGraphResponseDto = new UserFailedTriedWorkbookResponseDto();
        List<UserFailedTriedWorkbookR> userFailedTriedWorkbookListForRedis = new LinkedList<>();

        // 6. ????????? problem?????? responseDTO??? UserFailedTriedWorkbookRedis??? ??????
        for (String workbookId : userFailedTriedWorkbooks.keySet()) {
            WorkBookInfo workbook = workBookRepository.findByWorkbookId(workbookId);
            Map<String, Integer> userFailedTriedChapters = userFailedTriedWorkbooks.get(workbookId);
            Map<String, List<String>> userFailedTriedChaptersForRedis = userFailedTriedWorkbooksForRedis.get(
                workbookId);

            // responseDTO??? UserFailedTriedWorkbookRedis??? ???????????? ?????? chapter list ?????? ??????
            List<UserFailedTriedChapterDto> triedChapterList = new LinkedList<>();
            List<UserFailedTriedChapterR> userFailedTriedChapterRList = new LinkedList<>();

            // workbook??? chapter??? problem?????? responseDTO??? UserFailedTriedWorkbookRedis??? ??????
            for (String chapterId : userFailedTriedChapters.keySet()) {
                triedChapterList.add(new UserFailedTriedChapterDto(chapterId,
                    userFailedTriedChapters.get(chapterId)));

                // chapter??? chapterTitle??? ?????? chapterId??? ???????????? ?????? ????????? (?????? chapter title??? ???????????? ????????????)
                ChapterInfo chapter = chapterRepository.findByChapterId(chapterId).get();

                userFailedTriedChapterRList.add(
                    new UserFailedTriedChapterR(chapterId, chapter.getChapter(),
                        userFailedTriedChaptersForRedis.get(chapterId)));
            }

            userTriedProblemForGraphResponseDto.getFailedTriedWorkbookList().add(
                new UserFailedTriedWorkbookDto(workbook.getTitle(), triedChapterList));

            userFailedTriedWorkbookListForRedis.add(
                new UserFailedTriedWorkbookR(workbookId, workbook.getTitle(),
                    userFailedTriedChapterRList));
        }

        // 6. Redis??? userFailedTriedWorkbookRedis ?????? ??????
        userFailedTriedWorkbookRedisRepository.save(
            UserFailedTriedWorkbookRedis.builder()
                .userId(userId)
                // TTL ????????? ????????? ???????????? ?????? ????????????
                .expiration(300L)
                .userFailedTriedWorkbookList(userFailedTriedWorkbookListForRedis)
                .build()
        );

        return userTriedProblemForGraphResponseDto;
    }

}
