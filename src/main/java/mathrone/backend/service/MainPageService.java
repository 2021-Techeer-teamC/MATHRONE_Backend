package mathrone.backend.service;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import mathrone.backend.controller.dto.CarouselResponseDto;
import mathrone.backend.domain.UserWorkbookRelInfo;
import mathrone.backend.domain.WorkBookInfo;
import mathrone.backend.domain.WorkbookLevelInfo;
import mathrone.backend.domain.WorkbookRecommend;
import mathrone.backend.domain.userWorkbookData;
import mathrone.backend.repository.ChapterRepository;
import mathrone.backend.repository.LevelRepository;
import mathrone.backend.repository.ProblemRepository;
import mathrone.backend.repository.ProblemTryRepository;
import mathrone.backend.repository.UserWorkbookRelRepository;
import mathrone.backend.repository.WorkBookRecommendRepository;
import mathrone.backend.repository.WorkBookRepository;
import mathrone.backend.repository.WorkbookLevelRepository;
import org.springframework.stereotype.Service;

@Service
public class MainPageService {

    private final UserWorkbookRelRepository workBookRelRepository;
    private final WorkBookRepository workBookRepository;
    private final WorkbookLevelRepository workbookLevelRepository;
    private final WorkBookRecommendRepository workBookRecommendRepository;
    private final LevelRepository levelRepository;
    private final ProblemRepository problemRepository;
    private final ChapterRepository chapterRepository;
    private final ProblemTryRepository problemTryRepository;

    public MainPageService(UserWorkbookRelRepository workBookRelRepository,
        WorkBookRepository workBookRepository, WorkbookLevelRepository workbookLevelRepository,
        WorkBookRecommendRepository workBookRecommendRepository,
        LevelRepository levelRepository,
        ProblemRepository problemRepository, ChapterRepository chapterRepository,
        ProblemTryRepository problemTryRepository) {

        this.workBookRelRepository = workBookRelRepository;
        this.workBookRepository = workBookRepository;
        this.workbookLevelRepository = workbookLevelRepository;
        this.workBookRecommendRepository = workBookRecommendRepository;
        this.levelRepository = levelRepository;
        this.problemRepository = problemRepository;
        this.chapterRepository = chapterRepository;
        this.problemTryRepository = problemTryRepository;
    }

    public List<userWorkbookData> getTryingBook(int userId) {
        List<userWorkbookData> result = new ArrayList<userWorkbookData>();
        for (UserWorkbookRelInfo userWorkbookRelInfo : workBookRelRepository.findByUserIdAndWorkbookTry(
            userId)) {
            WorkBookInfo workBookInfo = workBookRepository.findByWorkbookId(
                userWorkbookRelInfo.getWorkbookId());
//            WorkbookLevelInfo workbookLevelInfo = workbookLevelRepository.findByWorkbookId(workBookInfo.getWorkbookId());
//            int low = workbookLevelInfo.getLowCnt();
//            int mid = workbookLevelInfo.getMidCnt();
//            int high = workbookLevelInfo.getHighCnt();
//            String b;
//            if (low > mid){
//                if (low > high)
//                    b = "1";
//                else
//                    b = "3";
//            }
//            else {
//                if (mid > high)
//                    b = "2";
//                else
//                    b = "3";
//            }
            userWorkbookData a = new userWorkbookData(workBookInfo.getWorkbookId(),
                workBookInfo.getTitle(), workBookInfo.getProfileImg(), workBookInfo.getPublisher(),
                getLevel(workBookInfo.getWorkbookId()), userWorkbookRelInfo.getWorkbookStar());
            result.add(a);
        }
        return result;
    }

    public List<CarouselResponseDto> getCarousel() {
        List<WorkbookRecommend> workbookRecommendList = workBookRecommendRepository.findAll();
        List<CarouselResponseDto> carouselResponseDtoList = new LinkedList<>();
        for (WorkbookRecommend workbookRecommend : workbookRecommendList) {
            WorkBookInfo workBookInfo = workBookRepository.findByWorkbookId(
                workbookRecommend.getWorkbookId());
            carouselResponseDtoList.add(CarouselResponseDto.builder()
                .workbookId(workBookInfo.getWorkbookId())
                .workbookTitle(workBookInfo.getTitle())
                .month(workBookInfo.getMonth())
                .year(workBookInfo.getYear())
                .profileImg(workBookInfo.getProfileImg())
                .intro(workbookRecommend.getIntro())
                .build());
        }
        return carouselResponseDtoList;
    }

    public List<userWorkbookData> getStarBook(int userId) {
        List<userWorkbookData> result = new ArrayList<userWorkbookData>();

        // userId??? ???????????? ?????? user??? ????????? ????????????
        List<UserWorkbookRelInfo> userWorkbook = workBookRelRepository.findByUserIdAndWorkbookStar(
            userId);

        List<WorkBookInfo> workbookList = new ArrayList<WorkBookInfo>();

        //?????? ?????? ???????????? ?????? ?????????
        for (UserWorkbookRelInfo userWorkbookRelInfo : userWorkbook) {
            WorkBookInfo w = workBookRepository.findByWorkbookId(
                userWorkbookRelInfo.getWorkbookId()); //?????? ???????????? ????????? ?????????
            workbookList.add(w);// ???????????? ??????
        }

        //????????? ????????? ?????????
        //workbookId,title,img,publisher,level,star
        for (WorkBookInfo wb : workbookList) {
            String level = getLevel(wb.getWorkbookId());
            userWorkbookData b = new userWorkbookData(wb.getWorkbookId(), wb.getTitle(),
                wb.getPublisher(), wb.getProfileImg(), level, true); //?????? true??? ?????? ?????????????????? ?????? true
            result.add(b);
        }

        return result;
    }


    public String getLevel(String workbookId) {
        //?????? ???????????? ???????????? ????????? ?????????
        WorkbookLevelInfo wb = levelRepository.findByWorkbookId(workbookId);

        //??? ???????????? ?????????
        int high = wb.getHighCnt();
        int mid = wb.getMidCnt();
        int low = wb.getLowCnt();

        //???????????? ?????????
        int maxValue = Math.max(high, Math.max(mid, low));

        if (maxValue == low) {
            return "1";
        } else if (maxValue == mid) {
            return "2";
        } else {
            return "3";
        }

    }

//    public List<RecentTryDto> getRecentTry(){
//        List<ProblemTry> problemList = problemTryRepository.findDistinctTop10(); // ?????? ??? 10??? ?????????
//        List<RecentTryDto> recentTryProblems = new ArrayList<RecentTryDto>();
//        for(int i = 0; i < 10; i++){
//            Problem problem = problemList.get(i).getProblem();
//            RecentTryDto recentTry = RecentTryDto.builder()
//                    .problemId(problem.getProblemId())
//                    .problemNum(problem.getProblemNum())
//                    .workbookTitle(workBookRepository.findByWorkbookId(problem.getChapterId()).getTitle())
//                    .level(problem.getLevelOfDiff())
//                    .subject(chapterRepository.findByChapterId(problem.getChapterId()).get().getSubject())
//                    .chapter(chapterRepository.findByChapterId(problem.getChapterId()).get().getChapter())
//                    .build();
//            recentTryProblems.add(recentTry);
//        }
//        return recentTryProblems;
//    }
}
