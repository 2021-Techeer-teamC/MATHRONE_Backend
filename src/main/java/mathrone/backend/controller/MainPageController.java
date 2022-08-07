package mathrone.backend.controller;

import mathrone.backend.controller.dto.RecentTryDto;
import mathrone.backend.controller.dto.CarouselResponseDto;
import mathrone.backend.domain.userWorkbookData;
import mathrone.backend.service.MainPageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController

public class MainPageController {

    private final MainPageService mainPageService;

    public MainPageController(MainPageService mainPageService) {
        this.mainPageService = mainPageService;
    }

    @GetMapping("/main/workbook/try")
    public List<userWorkbookData> getTryingList(
        @RequestParam(value = "userId", required = false) Integer userId) {
        return mainPageService.getTryingBook(userId);
    }

    @GetMapping("/main/carousel/list")
    public List<CarouselResponseDto> getCarousel() {
        return mainPageService.getCarousel();
    }

    @GetMapping("/main/workbook/star")
    public List<userWorkbookData> getStarList(
        @RequestParam(value = "userId", required = false) Integer userId) {
        return mainPageService.getStarBook(userId);
    }

    @GetMapping("/main/problem/try")
    public List<RecentTryDto> getRecentTry(){
        return mainPageService.getRecentTry();
    }
}
