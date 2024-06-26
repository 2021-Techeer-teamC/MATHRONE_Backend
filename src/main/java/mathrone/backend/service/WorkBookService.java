package mathrone.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.BookDetailDto;
import mathrone.backend.controller.dto.LevelInfoDto;
import mathrone.backend.controller.dto.UserEvaluateLevelRequestDto;
import mathrone.backend.controller.dto.UserWorkbookDataInterface;
import mathrone.backend.controller.dto.WorkbookDto;
import mathrone.backend.controller.dto.chapter.ChapterDto;
import mathrone.backend.controller.dto.chapter.ChapterGroup;
import mathrone.backend.controller.dto.interfaces.UserSolvedWorkbookResponseDtoInterface;
import mathrone.backend.domain.ChapterInfo;
import mathrone.backend.domain.PubCatPair;
import mathrone.backend.domain.Tag;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.domain.UserWorkbookRelInfo;
import mathrone.backend.domain.WorkBookInfo;
import mathrone.backend.domain.WorkbookLevelInfo;
import mathrone.backend.domain.bookContent;
import mathrone.backend.error.exception.CustomException;
import mathrone.backend.error.exception.ErrorCode;
import mathrone.backend.repository.ChapterRepository;
import mathrone.backend.repository.LevelRepository;
import mathrone.backend.repository.TagRepository;
import mathrone.backend.repository.UserInfoRepository;
import mathrone.backend.repository.UserWorkbookRelRepository;
import mathrone.backend.repository.WorkBookRepository;
import mathrone.backend.repository.WorkbookLevelRepository;
import mathrone.backend.util.TokenProviderUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkBookService {

    private final WorkBookRepository workBookRepository;
    private final LevelRepository levelRepository;
    private final ChapterRepository chapterRepository;
    private final TagRepository tagRepository;
    private final TokenProviderUtil tokenProviderUtil;
    private final UserWorkbookRelRepository userWorkbookRelRepository;
    private final WorkbookLevelRepository workbookLevelRepository;
    private final UserInfoRepository userInfoRepository;


    public List<WorkBookInfo> findWorkbook(String publisher, String category, Pageable pageable) {
        if (publisher.equals("all")) {
            return workBookRepository.findAll(pageable).getContent();
        } else if (category.equals("all")) {
            return workBookRepository.findAllByPublisher(publisher, pageable).getContent();
        } else {
            return workBookRepository.findAllByPublisherAndCategory(publisher, category, pageable)
                .getContent();
        }
    }

    public Long countWorkbook(String publisher, String category) {
        if (publisher.equals("all")) {
            return workBookRepository.count();
        } else if (category.equals("all")) {
            return workBookRepository.countByPublisher(publisher);
        } else {
            return workBookRepository.countByPublisherAndCategory(publisher, category);
        }
    }

    public String getLevel(WorkbookLevelInfo wb) {
        //해당 문제집의 레벨투표 정보를 가져옴
        //각 난이도별 투표수
        int high = wb.getHighCnt();
        int mid = wb.getMidCnt();
        int low = wb.getLowCnt();

        //투표수중 최대값
        int maxValue = Math.max(high, Math.max(mid, low));

        if (maxValue == low) {
            return "1";
        } else if (maxValue == mid) {
            return "2";
        } else {
            return "3";
        }

    }

    // 워크북 상세 페이지에 대한 정보를 불러옴
    @Transactional
    public BookDetailDto getWorkbookDetail(String workbookId, HttpServletRequest request) {
        Map<String, Set<ChapterDto>> arrMap = new HashMap<>(); // 그룹 별로 정리하기 위함
        Set<ChapterDto> list;
        Set<ChapterGroup> chapterGroups = new HashSet<>();
        List<Tag> tags = new ArrayList<>();
        boolean star = false;

        WorkBookInfo workBookInfo = workBookRepository.findByWorkbookId(workbookId);

        String accessToken = tokenProviderUtil.resolveToken(request);
        if(accessToken != null){    // 토큰이 존재할 경우 star값 가져오기
            UserInfo user = userInfoRepository.findById(
                    Integer.parseInt(tokenProviderUtil.getAuthentication(accessToken).getName())
            ).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            Optional<UserWorkbookRelInfo> relInfo = userWorkbookRelRepository.findByUserAndWorkbook(user, workBookInfo);
            if(relInfo.isPresent()){
                star = relInfo.get().getWorkbookStar();
            }
        }

        // 각 그룹별로 챕터 정리
        if (workBookInfo.getChapterId() != null) {
            for (String s : workBookInfo.getChapterId()) {
                ChapterInfo chapterInfo = chapterRepository.findByChapterId(s).
                    orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CHAPTER));
                ChapterDto chapters = ChapterDto.builder()
                    .id(chapterInfo.getChapterId())
                    .name(chapterInfo.getName())
                    .build();
                if (arrMap.containsKey(chapterInfo.getGroup())) {
                    list = arrMap.get(chapterInfo.getGroup());
                    list.add(chapters);
                } else {
                    list = new HashSet<>(List.of(chapters));
                    arrMap.put(chapterInfo.getGroup(), list);
                }
            }

            // 그룹별로 정리한 챕터 정보를 ChapterGroup 리스트 형식에 맞게 변환
            for (String key : arrMap.keySet()) {
                chapterGroups.add(
                    ChapterGroup.builder()
                        .group(key)
                        .chapters(arrMap.get(key))
                        .build());
            }
        }

        Long[] tagList = workBookInfo.getTags();
        if (tagList != null) {
            for (Long i : tagList) {
                if (tagRepository.findById(i).isPresent()) {
                    tags.add(tagRepository.findById(i).get());
                }
            }
        }

        LevelInfoDto levelInfo;
        Optional <WorkbookLevelInfo> workbookLevelInfo = workbookLevelRepository.findByWorkbookId(workbookId);

        if(accessToken != null) {   // access 토큰이 있다면 사용자 투표 결과도 return
            String userId = tokenProviderUtil.getAuthentication(accessToken).getName();
            UserInfo user = userInfoRepository.findByUserId(Integer.parseInt(userId));
            Optional<UserWorkbookRelInfo> userWorkbookRelInfo =
                    userWorkbookRelRepository.findByUserAndWorkbook(user, workBookInfo);
            if(userWorkbookRelInfo.isPresent()) // 평가를 진행했을 경우
                levelInfo = new LevelInfoDto(workbookLevelInfo.get().mostVotedLevel(), userWorkbookRelInfo.get().getVoteLevel()); // 사용자가 투표한 레벨
            else    // 사용자가 평가한 기록이 없을 경우
                levelInfo = new LevelInfoDto(workbookLevelInfo.get().mostVotedLevel(), 0);

        }
        else    // access 토큰이 없는 경우 최다 투표만 return
            levelInfo = new LevelInfoDto(workbookLevelInfo.get().mostVotedLevel());

        return BookDetailDto.builder()
            .workbookId(workBookInfo.getWorkbookId())
            .title(workBookInfo.getTitle())
            .summary("summary")
            .publisher(workBookInfo.getPublisher())
            .category(workBookInfo.getCategory())
            .thumbnail(workBookInfo.getThumbnail())
            .content(workBookInfo.getContent())
            .type(workBookInfo.getType())
            .year(workBookInfo.getYear())
            .month(workBookInfo.getMonth())
            .star(star)
            .chapterGroup(chapterGroups)
            .tags(tags)
            .level(levelInfo)
            .build();
    }

    public List<PubCatPair> getPublisherAndCategoryList() {
        return workBookRepository.findGroupByPublisherAndCategory();
    }

    public List<WorkbookDto> getBookList(HttpServletRequest request, Pageable paging,
        String publisher,
        String category,
        String sortType) {
        //1. 결과로 반환할 bookItem 리스트 (임시)
        List<WorkbookDto> result = new ArrayList<>();
        //파라미터 기반으로 결과 탐색
        List<WorkBookInfo> res = findWorkbook(publisher, category, paging);

        String accessToken = tokenProviderUtil.resolveToken(request);
        if (accessToken == null) {
            //결과에 level,like을 attach하여 리스트로 생성
            for (WorkBookInfo wb : res) {
                WorkbookLevelInfo workbookLevelInfo = levelRepository.findByWorkbookId(
                    wb.getWorkbookId());
                String level = getLevel(workbookLevelInfo);
                result.add(new WorkbookDto(wb.getWorkbookId(), wb.getTitle(), wb.getPublisher(),
                    wb.getThumbnail(), level, false));
            }
        } else {
            UserInfo user = userInfoRepository.findById(
                Integer.parseInt(tokenProviderUtil.getAuthentication(accessToken).getName())
            ).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            for (WorkBookInfo wb : res) {
                WorkbookLevelInfo workbookLevelInfo = levelRepository.findByWorkbookId(
                    wb.getWorkbookId());
                String level = getLevel(workbookLevelInfo);

                boolean star = userWorkbookRelRepository.findWorkbookStarByUserAndWorkbook(
                    user.getUserId(), workbookLevelInfo.getWorkbookId());

                result.add(new WorkbookDto(wb.getWorkbookId(), wb.getTitle(), wb.getPublisher(),
                    wb.getThumbnail(), level, star));
            }

        }

        //정렬 반영
        if (sortType.equals("level")) {//난이도 높은 순
            result.sort(Comparator.comparing(WorkbookDto::getLevel));
        }

        return result;
    }


    public List<bookContent> getWorkbookList() {
        //Nav bar
        List<bookContent> contentList = new ArrayList<>(); //output

        //group by 한 결과 받아오기
        List<PubCatPair> res = getPublisherAndCategoryList();

        //정렬 (출판사 순으로 정렬->같은 출판사끼리 모으기, 가나 2가지 기능)
        res.sort(Comparator.comparing(PubCatPair::getPublisher));

        //Map을 이용해서 출판사, 카테고리 리스트 로 정렬 -> 리스트는 key find effective x
        HashMap<String, LinkedList<String>> navList = new HashMap<>();

        String pastPub = "";
        LinkedList<String> valueList = new LinkedList<>();//카테고리 리스트

        int cnt = 0;
        for (PubCatPair wb : res) {

            //출판사, 카테고리(1걔)
            String p = wb.getPublisher();
            String c = wb.getCategory();

            if (cnt == 0) {
                pastPub = p;
            }

            if (pastPub.equals(p)) { //같은 것일때
                valueList.add(c);
            } else {
                navList.put(pastPub, new LinkedList<>(
                    valueList));//value에 값 추가 -> new로 새 객체에 담지 않으면 value 바뀔때마다 map값도 바뀜;
                valueList.clear();//재활용
                valueList.add(c);//이번턴 category
                pastPub = p;
            }
            cnt++;

        }
        navList.put(pastPub, valueList);//value에 값 추가

        //id를 위한 int 값
        long i = 0;
        for (Map.Entry<String, LinkedList<String>> entry : navList.entrySet()) {//java map순회 방법
            String p = entry.getKey(); //publisher(key)
            LinkedList<String> c = entry.getValue();//publisher에 해당하는 categories
            bookContent b = new bookContent(i++, p, c);//new bookContents
            contentList.add(b);//add output list
        }

        return contentList;
    }

    /**
     * jwt 여부에 따라 시도한 문제집 리스트
     * <p> token이 있는 경우 : 특정 유저가 시도한 문제집 리스트 반환
     * <p> token이 없는 경우 : 모든 유저가 시도한 문제집 리스트를 많이 시도한 순으로 6개 반환
     *
     * @return List<UserWorkbookDataInterface>
     */
    public List<UserWorkbookDataInterface> getTriedWorkbook(HttpServletRequest request) {
        String accessToken = tokenProviderUtil.resolveToken(request);
        if (accessToken != null) {
            return getUserTriedWorkbook(accessToken, request);
        } else {
            return userWorkbookRelRepository.findAllUserTriedWorkbook();
        }
    }

    /**
     * 특정 유저가 시도한 문제집 리스트 반환
     *
     * @param accessToken 유저 정보에 해당하는 jwt token
     * @return List<UserWorkbookDataInterface>
     */
    public List<UserWorkbookDataInterface> getUserTriedWorkbook(String accessToken,
        HttpServletRequest request) {
        // 유저의 token 유효성 검사
        if (!tokenProviderUtil.validateToken(accessToken, request)) {
            throw (CustomException) request.getAttribute("Exception");
        }

        int userId = Integer.parseInt(tokenProviderUtil.getAuthentication(accessToken).getName());

        return userWorkbookRelRepository.findUserTriedWorkbook(
            userId);
    }

    /**
     * jwt 여부에 따라 즐겨찾는 문제집 리스트 반환
     * <p> token이 있는 경우 : 특정 유저가 즐겨찾는 문제집 리스트 반환
     * <p> token이 없는 경우 : 모든 유저가 즐겨찾는 문제집 리스트를 많이 즐겨찾는 순으로 6개 반환
     *
     * @return List<UserWorkbookDataInterface>
     */
    public List<UserWorkbookDataInterface> getStarWorkbook(HttpServletRequest request) {
        String accessToken = tokenProviderUtil.resolveToken(request);
        if (accessToken != null) {
            return getUserStarWorkbook(accessToken, request);
        } else {
            return userWorkbookRelRepository.findAllUserStarWorkBook();
        }
    }

    /**
     * 모든 유저가 즐겨찾는 문제집 리스트를 많이 즐겨찾는 순으로 6개 반환
     *
     * @param accessToken 유저 정보에 해당하는 jwt token
     * @return List<UserWorkbookDataInterface>
     */
    private List<UserWorkbookDataInterface> getUserStarWorkbook(String accessToken,
        HttpServletRequest request) {
        // 유저의 token 유효성 검사
        if (!tokenProviderUtil.validateToken(accessToken, request)) {
            throw (CustomException) request.getAttribute("Exception");
        }

        int userId = Integer.parseInt(tokenProviderUtil.getAuthentication(accessToken).getName());

        return userWorkbookRelRepository.findUserStarWorkBook(
            userId);
    }

    /**
     * workbookId 유무에 따라 logic 처리
     * <p>- workbookId가 있는 경우 : 유저가 푼 문제집에 대한 풀이 정보
     * <p>- workbookId가 없는 경우 : 유저가 푼 모든 문제집에 대한 풀이 정보
     *
     * @param request    Http request
     * @param byWorkbookId 문제집 Id
     * @return List<UserSolvedWorkbookResponseDtoInterface>
     */
    public Set<UserSolvedWorkbookResponseDtoInterface> trackSolvedWorkbooks(
        HttpServletRequest request, Optional<String> byWorkbookId) {

        String accessToken = tokenProviderUtil.resolveToken(request);
        if (!tokenProviderUtil.validateToken(accessToken, request)) {
            throw (CustomException) request.getAttribute("Exception");
        }

        int userId = Integer.parseInt(tokenProviderUtil.getAuthentication(accessToken).getName());
        if (byWorkbookId.isEmpty()){
            // 유저가 푼 모든 문제집에 대한 풀이 정보 tracking
            return workBookRepository.findByUserSolvedAllWorkbook(userId);
        } else {
            // 문제집 Id의 유효성 검증
            WorkBookInfo workbook = workBookRepository.findById(byWorkbookId.get()).
                orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_WORKBOOK));

            // 특정 문제집에 대한 유저의 풀이 정보 tracking
            return workBookRepository.findByUserSolvedWorkbook(
                workbook.getWorkbookId(), userId);
        }
    }


    /**
     * user의 Workbook 평가 요청 처리
     *
     * @param request                     http request
     * @param userEvaluateLevelRequestDto 평가 요청에 필요한 정보 dto
     */
    @Transactional
    public void evaluateWorkbook(HttpServletRequest request,
        UserEvaluateLevelRequestDto userEvaluateLevelRequestDto) {
        String accessToken = tokenProviderUtil.resolveToken(request);

        if (!tokenProviderUtil.validateToken(accessToken, request)) {
            throw (CustomException) request.getAttribute("Exception");
        }
        int userId = Integer.parseInt(tokenProviderUtil.getAuthentication(accessToken).getName());
        UserInfo user = userInfoRepository.getById(userId);

        Optional<WorkbookLevelInfo> isWorkbookLevelInfo = workbookLevelRepository.findByWorkbookId(
            userEvaluateLevelRequestDto.getWorkbookId());

        Optional<WorkBookInfo> workBookInfo = workBookRepository.findById(userEvaluateLevelRequestDto.getWorkbookId());

        if (workBookInfo.isEmpty() || isWorkbookLevelInfo.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND_WORKBOOK);
        }
        UserWorkbookRelInfo userWorkbookRel;
        Optional<UserWorkbookRelInfo> userWorkbookRelInfo = userWorkbookRelRepository.findByUserAndWorkbook(
                user, workBookInfo.get());  // 유저가 문제집에 대해 평가

        if(userWorkbookRelInfo.isEmpty()){  // 없을 경우 새로 생성
            userWorkbookRel = userWorkbookRelRepository.save(UserWorkbookRelInfo.builder()
                    .user(user)
                    .workbook(workBookInfo.get())
                    .build());
        }
        else    // 있다면 기존것 사용
            userWorkbookRel = userWorkbookRelInfo.get();

        WorkbookLevelInfo workbookLevelInfo = isWorkbookLevelInfo.get();
        int level = userEvaluateLevelRequestDto.getLevel(); // 사용자가 투표한 레벨

        if(userWorkbookRel.getVoteLevel() != 0){    // 이미 투표가 되어있다면
            if(userWorkbookRel.getVoteLevel() == level)
                return; // 같은 값으로 변경 요청 들어오면 return
            updateLevelCount(userWorkbookRel.getVoteLevel(), true, workbookLevelInfo);
            // 기존 투표 취소
        }
        updateLevelCount(level, false, workbookLevelInfo);  // 새롭게 투표한 결과 저장
        userWorkbookRel.updateVote(level);  // 사용자가 어떤 레벨에 투표했는지
    }

    /**
     * user의 Workbook 평가 요청 처리
     *
     * @param level  저장 혹은 취소할 레벨
     * @param isExist 기투표 여부 (true면 취소 메서드)
     * @param workbookLevelInfo 워크북 총 투표 결과 인스턴스
     */
    private void updateLevelCount(int level, boolean isExist, WorkbookLevelInfo workbookLevelInfo){
        int temp = 1;
        if(isExist) // 취소 매서드일 경우 -1
            temp *= -1;
        switch (level) { //  투표 저장 메서드
            case 1:
                workbookLevelInfo.updateLowCount(workbookLevelInfo.getLowCnt() + temp);
                break;
            case 2:
                workbookLevelInfo.updateMidCount(workbookLevelInfo.getMidCnt() + temp);
                break;
            case 3:
                workbookLevelInfo.updateHighCount(workbookLevelInfo.getHighCnt() + temp);
                break;
            default:
                throw new CustomException(ErrorCode.INVALID_LEVEL_VALUE);
        }
    }

    /**
     * 유저에 대한 workbook의 즐겨찾기 추가 or 삭제
     *
     * @param request    http request
     * @param workbookId workbook Id
     */
    @Transactional
    public void starWorkbook(HttpServletRequest request, String workbookId) {
        String accessToken = tokenProviderUtil.resolveToken(request);

        if (!tokenProviderUtil.validateToken(accessToken, request)) {
            throw (CustomException) request.getAttribute("Exception");
        }
        int userId = Integer.parseInt(tokenProviderUtil.getAuthentication(accessToken).getName());

        UserInfo user = userInfoRepository.getById(userId);
        WorkBookInfo workbook = workBookRepository.findById(workbookId).orElseThrow(() ->
            new CustomException(ErrorCode.NOT_FOUND_WORKBOOK));

        Optional<UserWorkbookRelInfo> byUserAndWorkbook = userWorkbookRelRepository.findByUserAndWorkbook(
            user, workbook);

        if (byUserAndWorkbook.isEmpty()) {
            userWorkbookRelRepository.save(UserWorkbookRelInfo.builder()
                .workbook(workbook)
                .user(user)
                .workbookStar(true).build());
        } else {
            UserWorkbookRelInfo userWorkbookRelInfo = byUserAndWorkbook.get();
            userWorkbookRelInfo.updateStar(true);
        }
    }

    @Transactional
    public void deleteStarWorkbook(HttpServletRequest request, String workbookId) {
        String accessToken = tokenProviderUtil.resolveToken(request);

        if (!tokenProviderUtil.validateToken(accessToken, request)) {
            throw (CustomException) request.getAttribute("Exception");
        }

        int userId = Integer.parseInt(tokenProviderUtil.getAuthentication(accessToken).getName());

        UserInfo user = userInfoRepository.getById(userId);
        WorkBookInfo workbook = workBookRepository.findById(workbookId).orElseThrow(() ->
            new CustomException(ErrorCode.NOT_FOUND_WORKBOOK));

        Optional<UserWorkbookRelInfo> byUserAndWorkbook = userWorkbookRelRepository.findByUserAndWorkbook(
            user, workbook);

        if (byUserAndWorkbook.isEmpty()) {
            userWorkbookRelRepository.save(UserWorkbookRelInfo.builder()
                .workbook(workbook)
                .user(user)
                .workbookStar(false).build());
        } else {
            UserWorkbookRelInfo userWorkbookRelInfo = byUserAndWorkbook.get();
            userWorkbookRelInfo.updateStar(false);
        }

    }
}
