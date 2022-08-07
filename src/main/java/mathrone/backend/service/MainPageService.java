package mathrone.backend.service;


import mathrone.backend.controller.dto.RecentTryDto;
import mathrone.backend.controller.dto.CarouselResponseDto;
import mathrone.backend.domain.*;
import mathrone.backend.repository.ChapterRepository;
import mathrone.backend.repository.LevelRepository;
import mathrone.backend.repository.ProblemRepository;
import mathrone.backend.repository.ProblemTryRespository;
import mathrone.backend.repository.UserWorkbookRelRepository;
import mathrone.backend.repository.WorkBookRecommendRepository;
import mathrone.backend.repository.WorkBookRepository;
import mathrone.backend.repository.WorkbookLevelRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
public class MainPageService {
    private final UserWorkbookRelRepository workBookRelRepository;
    private final WorkBookRepository workBookRepository;
    private final WorkbookLevelRepository workbookLevelRepository;
    private final WorkBookRecommendRepository workBookRecommendRepository;
    private final LevelRepository levelRepository;
    private final ProblemRepository problemRepository;
    private final ChapterRepository chapterRepository;
    private final ProblemTryRespository problemTryRespository;

    public MainPageService(UserWorkbookRelRepository workBookRelRepository,
            WorkBookRepository workBookRepository, WorkbookLevelRepository workbookLevelRepository,
            WorkBookRecommendRepository workBookRecommendRepository,
            LevelRepository levelRepository,
            ProblemRepository problemRepository, ChapterRepository chapterRepository,
            ProblemTryRespository problemTryRespository) {

        this.workBookRelRepository = workBookRelRepository;
        this.workBookRepository = workBookRepository;
        this.workbookLevelRepository = workbookLevelRepository;
        this.workBookRecommendRepository = workBookRecommendRepository;
        this.levelRepository = levelRepository;
        this.problemRepository = problemRepository;
        this.chapterRepository = chapterRepository;
        this.problemTryRespository = problemTryRespository;
    }

    public List<userWorkbookData> getTryingBook(int userId){
        List<userWorkbookData> result = new ArrayList<userWorkbookData>();
        for(UserWorkbookRelInfo userWorkbookRelInfo: workBookRelRepository.findByUserIdAndWorkbookTry(userId)){
            WorkBookInfo workBookInfo = workBookRepository.findByWorkbookId(userWorkbookRelInfo.getWorkbookId());
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
            userWorkbookData a = new userWorkbookData(workBookInfo.getWorkbookId(), workBookInfo.getTitle(), workBookInfo.getProfileImg(), workBookInfo.getPublisher(), getLevel(workBookInfo.getWorkbookId()), userWorkbookRelInfo.getWorkbookStar());
            result.add(a);
        }
        return result;
    }

    public List<CarouselResponseDto> getCarousel() {
        List<WorkbookRecommend> workbookRecommendList = workBookRecommendRepository.findAll();
        List<CarouselResponseDto> carouselResponseDtoList = new LinkedList<>();
        for (WorkbookRecommend workbookRecommend : workbookRecommendList){
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

    public List<userWorkbookData> getStarBook(int userId){
        List<userWorkbookData> result = new ArrayList<userWorkbookData>();

        // userId를 기반으로 해당 user의 정보를 찾아오기
        List<UserWorkbookRelInfo> userWorkbook = workBookRelRepository.findByUserIdAndWorkbookStar(userId);

        List<WorkBookInfo> workbookList = new ArrayList<WorkBookInfo>();

        //별이 찍힌 문제집의 정보 리스트
        for(UserWorkbookRelInfo userWorkbookRelInfo: userWorkbook){
            WorkBookInfo w = workBookRepository.findByWorkbookId(userWorkbookRelInfo.getWorkbookId()); //해당 문제집의 정보를 가져옴
            workbookList.add(w);// 리스트에 저장
        }


        //원하는 정보만 남기기
        //workbookId,title,img,publisher,level,star
        for(WorkBookInfo wb: workbookList){
            String level = getLevel(wb.getWorkbookId());
            userWorkbookData b = new userWorkbookData(wb.getWorkbookId(), wb.getTitle(), wb.getPublisher(), wb.getProfileImg(),level, true); //이미 true인 값에 대해서만해서 항상 true
            result.add(b);
        }

        return result;
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
    
    public List<RecentTryDto> getRecentTry(){
        List<ProblemTry> problemList = problemTryRespository.findDistinctTop10(); // 최근 푼 10개 가져옴
        List<RecentTryDto> recentTryProblems = new ArrayList<RecentTryDto>();
        for(int i = 0; i < 10; i++){
            Problem problem = problemList.get(i).getProblem();
            RecentTryDto recentTry = RecentTryDto.builder()
                    .problemId(problem.getProblemId())
                    .problemNum(problem.getProblemNum())
                    .workbookTitle(workBookRepository.findByWorkbookId(problem.getChapterId()).getTitle())
                    .level(problem.getLevelOfDiff())
                    .subject(chapterRepository.findByChapterId(problem.getChapterId()).get().getSubject())
                    .chapter(chapterRepository.findByChapterId(problem.getChapterId()).get().getChapter())
                    .build();
            recentTryProblems.add(recentTry);
        }
        return recentTryProblems;
    }
}
