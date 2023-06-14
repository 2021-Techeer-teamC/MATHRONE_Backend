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
import mathrone.backend.controller.dto.UserEvaluateLevelRequestDto;
import mathrone.backend.controller.dto.interfaces.UserSolvedWorkbookResponseDtoInterface;
import mathrone.backend.domain.Problem;
import mathrone.backend.domain.PubCatPair;
import mathrone.backend.domain.WorkBookInfo;
import mathrone.backend.domain.WorkbookLevelInfo;
import mathrone.backend.domain.bookContent;
import mathrone.backend.domain.bookItem;
import mathrone.backend.error.exception.CustomException;
import mathrone.backend.error.exception.ErrorCode;
import mathrone.backend.repository.LevelRepository;
import mathrone.backend.repository.ProblemRepository;
import mathrone.backend.repository.UserWorkbookRepository;
import mathrone.backend.repository.WorkBookRepository;
import mathrone.backend.repository.WorkbookLevelRepository;
import mathrone.backend.util.TokenProviderUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class WorkBookService {

    private final WorkBookRepository workBookRepository;
    private final LevelRepository levelRepository;
    private final UserWorkbookRepository userWorkbookRepository;
    private final ProblemRepository problemRepository;
    private final TokenProviderUtil tokenProviderUtil;
    private final WorkbookLevelRepository workbookLevelRepository;

    //생성자

    public WorkBookService(WorkBookRepository workBookRepository,
        ProblemRepository problemRepository, LevelRepository levelRepository,
        UserWorkbookRepository userWorkbookRepository, TokenProviderUtil tokenProviderUtil,
        WorkbookLevelRepository workbookLevelRepository) {
        this.workBookRepository = workBookRepository;
        this.levelRepository = levelRepository;
        this.userWorkbookRepository = userWorkbookRepository;
        this.problemRepository = problemRepository;
        this.tokenProviderUtil = tokenProviderUtil;
        this.workbookLevelRepository = workbookLevelRepository;
    }


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

    public String getLevel(String workbookId) {
        //해당 문제집의 레벨투표 정보를 가져옴
        WorkbookLevelInfo wb = levelRepository.findByWorkbookId(workbookId);

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

    public Long getStar(String workbookId) {
        return userWorkbookRepository.countByWorkbookIdAndWorkbookStar(workbookId,
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
            String level = getLevel(wb.getWorkbookId());
            Long star = getStar(wb.getWorkbookId());
            bookItem b = new bookItem(wb.getWorkbookId(), wb.getTitle(), wb.getPublisher(),
                wb.getProfileImg(), level, star);
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
     * @param request http request
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
            case 2:
                workbookLevelInfo.updateMidCount(workbookLevelInfo.getMidCnt() + 1);
            case 3:
                workbookLevelInfo.updateHighCount(workbookLevelInfo.getHighCnt() + 1);
        }
    }
}
