package mathrone.backend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mathrone.backend.domain.Problem;
import mathrone.backend.domain.PubCatPair;
import mathrone.backend.domain.UserWorkbookData;
import mathrone.backend.domain.WorkBookInfo;
import mathrone.backend.domain.WorkbookLevelInfo;
import mathrone.backend.domain.bookContent;
import mathrone.backend.domain.bookItem;
import mathrone.backend.error.exception.CustomException;
import mathrone.backend.repository.LevelRepository;
import mathrone.backend.repository.ProblemRepository;
import mathrone.backend.repository.UserWorkbookRepository;
import mathrone.backend.repository.WorkBookRepository;
import mathrone.backend.util.TokenProviderUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkBookService {

    private final WorkBookRepository workBookRepository;
    private final LevelRepository levelRepository;
    private final UserWorkbookRepository userWorkbookRepository;
    private final ProblemRepository problemRepository;
    private final TokenProviderUtil tokenProviderUtil;

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
     * jwt 여부에 따라 시도한 문제집 리스트 반환 logic
     *
     * @return List<userWorkbookData>
     */
    public List<UserWorkbookData> getTryingBook(HttpServletRequest request) {
        String accessToken = tokenProviderUtil.resolveToken(request);
        if (accessToken != null) {
            return getUserTryBook(accessToken, request);
        } else {
            return getAllUserTriedBook();
        }
    }

    /**
     * 특정 유저가 시도한 문제집 리스트 logic
     *
     * @param accessToken 유저 정보에 해당하는 jwt token
     * @return List<userWorkbookData>
     */
    public List<UserWorkbookData> getUserTryBook(String accessToken, HttpServletRequest request) {
        // 유저의 token 유효성 검사
        if (!tokenProviderUtil.validateToken(accessToken, request)) {
            throw (CustomException) request.getAttribute("Exception");
        }

        int userId = Integer.parseInt(tokenProviderUtil.getAuthentication(accessToken).getName());

        List<WorkBookInfo> userTriedWorkbookList = workBookRepository.findUserTriedWorkbook(
            userId);

        // user가 시도한 문제집이 있는 경우
        return getUserWorkbookDataList(userTriedWorkbookList, false);
    }

    /**
     * 모든 유저가 시도한 문제집 리스트를 많이 시도한 순으로 6개 반환
     *
     * @return List<userWorkbookData>
     */
    private List<UserWorkbookData> getAllUserTriedBook() {
        List<WorkBookInfo> allUserTriedWorkBookList = workBookRepository.findAllUserTriedWorkbook();

        return getUserWorkbookDataList(allUserTriedWorkBookList, false);
    }

    /**
     * jwt 여부에 따라 즐겨찾는 문제집 리스트 반환
     *
     * @return List<userWorkbookData>
     */
    public List<UserWorkbookData> getStarBook(HttpServletRequest request) {
        String accessToken = tokenProviderUtil.resolveToken(request);
        if (accessToken != null) {
            return getUserStarBook(accessToken, request);
        } else {
            return getAllUserStarBook();
        }
    }

    /**
     * 특정 유저가 즐겨찾는 문제집 리스트 반환
     *
     * @return List<userWorkbookData>
     */
    private List<UserWorkbookData> getAllUserStarBook() {
        List<WorkBookInfo> userStarBookList = workBookRepository.findAllUserStarWorkBook();

        return getUserWorkbookDataList(userStarBookList, false);
    }

    /**
     * 모든 유저가 즐겨찾는 문제집 리스트를 많이 즐겨찾는 순으로 6개 반환
     *
     * @param accessToken 유저 정보에 해당하는 jwt token
     * @return List<userWorkbookData>
     */
    private List<UserWorkbookData> getUserStarBook(String accessToken, HttpServletRequest request) {
        // 유저의 token 유효성 검사
        if (!tokenProviderUtil.validateToken(accessToken, request)) {
            throw (CustomException) request.getAttribute("Exception");
        }

        int userId = Integer.parseInt(tokenProviderUtil.getAuthentication(accessToken).getName());

        List<WorkBookInfo> userStarBookList = workBookRepository.findUserStarWorkBook(
            userId);

        return getUserWorkbookDataList(userStarBookList, false);
    }


    /**
     * UserWorkbookRelInfo List를 토대로 UserWorkbookData 가공
     *
     * @param userWorkbookRelList 유저와 문제집 사이의 연관 정보(즐겨찾기, 시도 여부 등)를 가지는 list
     * @return List<UserWorkbookData>
     */
    private List<UserWorkbookData> getUserWorkbookDataList(
        List<WorkBookInfo> userWorkbookRelList, boolean isStar) {
        List<UserWorkbookData> result = new ArrayList<UserWorkbookData>();

        for (WorkBookInfo workBookInfo : userWorkbookRelList) {
            UserWorkbookData userWorkbookData = new UserWorkbookData(workBookInfo.getWorkbookId(),
                workBookInfo.getTitle(), workBookInfo.getProfileImg(), workBookInfo.getPublisher(),
                getLevel(workBookInfo.getWorkbookId()), isStar);
            result.add(userWorkbookData);
        }

        return result;
    }
}
