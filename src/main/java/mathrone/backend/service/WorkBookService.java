package mathrone.backend.service;

import mathrone.backend.domain.*;
import mathrone.backend.repository.LevelRepository;
import mathrone.backend.repository.ProblemRepository;
import mathrone.backend.repository.UserWorkbookRepository;
import mathrone.backend.repository.WorkBookRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import java.util.*;

@Service
public class WorkBookService {

    private final WorkBookRepository workBookRepository;
    private final LevelRepository levelRepository;
    private final UserWorkbookRepository userWorkbookRepository;
    private final ProblemRepository problemRepository;

    //생성자

    public WorkBookService(WorkBookRepository workBookRepository, ProblemRepository problemRepository, LevelRepository levelRepository, UserWorkbookRepository userWorkbookRepository){
        this.workBookRepository = workBookRepository;
        this.levelRepository = levelRepository;
        this.userWorkbookRepository = userWorkbookRepository;
        this.problemRepository = problemRepository;
    }


    public List<WorkBookInfo> findWorkbook(String publisher, String category, Pageable pageable) {
        if (publisher.equals("all"))
            return workBookRepository.findAll(pageable).getContent();
        else if(category.equals("all"))
            return workBookRepository.findAllByPublisher(publisher, pageable).getContent();
        else
            return workBookRepository.findAllByPublisherAndCategory(publisher, category, pageable).getContent();
    }

    public Long countWorkbook(String publisher, String category) {
        if (publisher.equals("all"))
            return workBookRepository.count();
        else if(category.equals("all"))
            return workBookRepository.countByPublisher(publisher);
        else
            return workBookRepository.countByPublisherAndCategory(publisher, category);
    }

    public String getLevel(String workbookId){
        //해당 문제집의 레벨투표 정보를 가져옴
        WorkbookLevelInfo wb = levelRepository.findByWorkbookId(workbookId);

        //각 난이도별 투표수
        int high = wb.getHighCnt();
        int mid = wb.getMidCnt();
        int low = wb.getLowCnt();

        //투표수중 최대값
        int maxValue = Math.max(high,Math.max(mid,low));

        if(maxValue==low) return "1";
        else if(maxValue==mid) return "2";
        else return "3";

    }

    public Long getStar(String workbookId){
        return userWorkbookRepository.countByWorkbookIdAndWorkbookStar(workbookId, true); //좋아요 표시 눌린것만
    }


    public List<Problem> findProblem(String workbookId, String chapterId){
        return problemRepository.findByWorkbookIdAndChapterId(workbookId, chapterId);
    }

    public List<PubCatPair> getPublisherAndCategoryList(){
        return workBookRepository.findGroupByPublisherAndCategory();
    }

    public List<bookItem> getBookList(Pageable paging, String publisher, String category,String sortType){

        //1. 결과로 반환할 bookItem 리스트 (임시)
        List<bookItem> result = new ArrayList<bookItem>();

        //파라미터 기반으로 결과 탐색
        List<WorkBookInfo> res = findWorkbook(publisher,category,paging);

        //결과에 level,like을 attach하여 리스트로 생성
        for (WorkBookInfo wb: res) {
            String level = getLevel(wb.getWorkbookId());
            Long star = getStar(wb.getWorkbookId());
            bookItem b = new bookItem(wb.getWorkbookId(), wb.getTitle(), wb.getPublisher(), wb.getProfileImg(),level, star);
            result.add(b);
        }

        //정렬 반영
        if(sortType.equals("star")){//좋아요 높은 순
            Collections.sort(result, new Comparator<bookItem>() {
                public int compare(bookItem o1, bookItem o2) {
                    return o2.getStar().compareTo(o1.getStar());
                }
            });
        }
        else{//level 난이도 높은 순
            Collections.sort(result, new Comparator<bookItem>() {
                public int compare(bookItem o1, bookItem o2) {
                    return o2.getLevel().compareTo(o1.getLevel());
                }
            });
        }

        return result;
    }


    public List<bookContent> getWorkbookList(){
        //Nav bar
        List<bookContent> contentList = new ArrayList<bookContent>(); //output

        //group by 한 결과 받아오기
        List<PubCatPair> res = getPublisherAndCategoryList();

        //정렬 (출판사 순으로 정렬->같은 출판사끼리 모으기, 가나 2가지 기능)
        Collections.sort(res, Comparator.comparing(p -> p.getPublisher()));

        //Map을 이용해서 출판사, 카테고리 리스트 로 정렬 -> 리스트는 key find effective x
        HashMap<String, LinkedList<String>> navList = new HashMap<>();

        String pastPub="";
        LinkedList<String> valueList = new LinkedList<>();//카테고리 리스트



        int cnt=0;
        for (PubCatPair wb: res) {

            //출판사, 카테고리(1걔)
            String p = wb.getPublisher();
            String c = wb.getCategory();

            if(cnt==0){
                pastPub=p;
            }

            if(pastPub.equals(p)){ //같은 것일때
                valueList.add(c);
            }
            else{
                navList.put(pastPub, new LinkedList<>(valueList));//value에 값 추가 -> new로 새 객체에 담지 않으면 value 바뀔때마다 map값도 바뀜;
                valueList.clear();//재활용
                valueList.add(c);//이번턴 category
                pastPub=p;
            }
            cnt++;

        }
        navList.put(pastPub,valueList);//value에 값 추가

        //id를 위한 int 값
        long i=0;
        for (Map.Entry<String, LinkedList<String>> entry:navList.entrySet()) {//java map순회 방법
            String p = entry.getKey(); //publisher(key)
            LinkedList<String> c = entry.getValue();//publisher에 해당하는 categories
            bookContent b = new bookContent(i++,p,c);//new bookContents
            contentList.add(b);//add output list
        }

        return contentList;
    }

}
