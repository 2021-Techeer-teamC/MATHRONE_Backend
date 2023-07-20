package mathrone.backend.service;


import java.util.LinkedList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.CarouselResponseDto;
import mathrone.backend.domain.WorkBookInfo;
import mathrone.backend.domain.WorkbookRecommend;
import mathrone.backend.repository.WorkBookRecommendRepository;
import mathrone.backend.repository.WorkBookRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MainPageService {

    private final WorkBookRepository workBookRepository;
    private final WorkBookRecommendRepository workBookRecommendRepository;

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

//    public List<RecentTryDto> getRecentTry(){
//        List<ProblemTry> problemList = problemTryRepository.findDistinctTop10(); // 최근 푼 10개 가져옴
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
