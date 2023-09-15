package mathrone.backend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.BookDetailDto;
import mathrone.backend.controller.dto.BookDetailDto.ChapterGroup;
import mathrone.backend.controller.dto.BookDetailDto.Chapters;
import mathrone.backend.controller.dto.UserEvaluateLevelRequestDto;
import mathrone.backend.controller.dto.UserWorkbookDataInterface;
import mathrone.backend.controller.dto.interfaces.UserSolvedWorkbookResponseDtoInterface;
import mathrone.backend.domain.ChapterInfo;
import mathrone.backend.domain.Problem;
import mathrone.backend.domain.PubCatPair;
import mathrone.backend.domain.Tag;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.domain.UserWorkbookRelInfo;
import mathrone.backend.domain.WorkBookInfo;
import mathrone.backend.domain.WorkbookLevelInfo;
import mathrone.backend.domain.bookContent;
import mathrone.backend.domain.bookItem;
import mathrone.backend.error.exception.CustomException;
import mathrone.backend.error.exception.ErrorCode;
import mathrone.backend.repository.ChapterRepository;
import mathrone.backend.repository.LevelRepository;
import mathrone.backend.repository.ProblemRepository;
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
    private final ProblemRepository problemRepository;
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
    public BookDetailDto getWorkbookDetail(String workbookId) {
        Map<String, List<Chapters>> arrMap = new HashMap<>(); // 그룹 별로 정리하기 위함
        List<Chapters> list = new ArrayList<>();
        List<ChapterGroup> chapterGroups = new ArrayList<>();
        List<Tag> tags = new ArrayList<>();

        WorkBookInfo workBookInfo = workBookRepository.findByWorkbookId(workbookId);

        // 각 그룹별로 챕터 정리
        if (workBookInfo.getChapterId() != null) {
            for (String s : workBookInfo.getChapterId()) {
                ChapterInfo chapterInfo = chapterRepository.findByChapterId(s).get();
                Chapters chapters = Chapters.builder()
                    .id(chapterInfo.getChapterId())
                    .name(chapterInfo.getName())
                    .build();
                if (arrMap.containsKey(chapterInfo.getGroup())) {
                    list = arrMap.get(chapterInfo.getGroup());
                    list.add(chapters);
                } else {
                    list.clear();
                    list.add(chapters);
                }
                arrMap.put(chapterInfo.getGroup(), list);
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
            .chapterGroup(chapterGroups)
            .tags(tags)
            .build();
    }

    public Long getStar(String workbookId) {
        Optional<WorkBookInfo> isWorkbook = workBookRepository.findById(workbookId);

        // Workbook이 없는 경우 error처리
        if (isWorkbook.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND_WORKBOOK);
        }

        return userWorkbookRelRepository.countByWorkbookAndWorkbookStar(isWorkbook.get(),
            true); //좋아요 표시 눌린것만
    }


    public List<Problem> findProblem(String workbookId, String chapterId) {
        return problemRepository.findByWorkbookIdAndChapterId(workbookId, chapterId);
    }

    public List<PubCatPair> getPublisherAndCategoryList() {
        return workBookRepository.findGroupByPublisherAndCategory();
    }

    public List<bookItem> getBookList(Pageable paging, String publisher, String category,
        String sortType) {

        //1. 결과로 반환할 bookItem 리스트 (임시)
        List<bookItem> result = new ArrayList<bookItem>();

        //파라미터 기반으로 결과 탐색
        List<WorkBookInfo> res = findWorkbook(publisher, category, paging);

        //결과에 level,like을 attach하여 리스트로 생성
        for (WorkBookInfo wb : res) {
            WorkbookLevelInfo workbookLevelInfo = levelRepository.findByWorkbookId(
                wb.getWorkbookId());
            String level = getLevel(workbookLevelInfo);
            Long star = getStar(wb.getWorkbookId());
            bookItem b = new bookItem(wb.getWorkbookId(), wb.getTitle(), wb.getPublisher(),
                wb.getThumbnail(), level, star);
            result.add(b);
        }

        //정렬 반영
        if (sortType.equals("star")) {//좋아요 높은 순
            Collections.sort(result, new Comparator<bookItem>() {
                public int compare(bookItem o1, bookItem o2) {
                    return o2.getStar().compareTo(o1.getStar());
                }
            });
        } else {//level 난이도 높은 순
            Collections.sort(result, new Comparator<bookItem>() {
                public int compare(bookItem o1, bookItem o2) {
                    return o2.getLevel().compareTo(o1.getLevel());
                }
            });
        }

        return result;
    }


    public List<bookContent> getWorkbookList() {
        //Nav bar
        List<bookContent> contentList = new ArrayList<bookContent>(); //output

        //group by 한 결과 받아오기
        List<PubCatPair> res = getPublisherAndCategoryList();

        //정렬 (출판사 순으로 정렬->같은 출판사끼리 모으기, 가나 2가지 기능)
        Collections.sort(res, Comparator.comparing(p -> p.getPublisher()));

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
     * @param workbookId 문제집 Id
     * @return List<UserSolvedWorkbookResponseDtoInterface>
     */
    public List<UserSolvedWorkbookResponseDtoInterface> trackSolvedWorkbook(
        HttpServletRequest request,
        Optional<String> workbookId) {

        String accessToken = tokenProviderUtil.resolveToken(request);
        if (!tokenProviderUtil.validateToken(accessToken, request)) {
            throw (CustomException) request.getAttribute("Exception");
        }
        int userId = Integer.parseInt(tokenProviderUtil.getAuthentication(accessToken).getName());

        // 특정 문제집에 대한 유저의 풀이 정보 tracking
        if (workbookId.isPresent()) {
            Optional<WorkBookInfo> byWorkbook = workBookRepository.findById(workbookId.get());
            if (byWorkbook.isEmpty()) {
                throw new CustomException(ErrorCode.NOT_FOUND_WORKBOOK);
            }
            return workBookRepository.findByUserSolvedWorkbook(
                workbookId.get(), userId);
        } else {
            // 유저가 푼 모든 문제집에 대한 풀이 정보 tracking
            return workBookRepository.findByUserSolvedAllWorkbook(userId);
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

        Optional<WorkbookLevelInfo> isWorkbookLevelInfo = workbookLevelRepository.findByWorkbookId(
            userEvaluateLevelRequestDto.getWorkbookId());

        if (isWorkbookLevelInfo.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND_WORKBOOK);
        }

        WorkbookLevelInfo workbookLevelInfo = isWorkbookLevelInfo.get();
        int level = userEvaluateLevelRequestDto.getLevel();

        switch (level) {
            case 1:
                workbookLevelInfo.updateLowCount(workbookLevelInfo.getLowCnt() + 1);
                break;
            case 2:
                workbookLevelInfo.updateMidCount(workbookLevelInfo.getMidCnt() + 1);
                break;
            case 3:
                workbookLevelInfo.updateHighCount(workbookLevelInfo.getHighCnt() + 1);
                break;
            default:
                throw new CustomException(ErrorCode.INVALID_LEVEL_VALUE);

        }
    }

    /**
     * 유저에 대한 workbook의 즐겨찾기 추가 or 삭제
     *
     * @param request       http request
     * @param workbookId    workbook Id
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

        if (byUserAndWorkbook.isEmpty()){
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

        if (byUserAndWorkbook.isEmpty()){
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
