package mathrone.backend.controller;

import java.util.List;
import mathrone.backend.controller.dto.CarouselResponseDto;
import mathrone.backend.service.MainPageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/main")
public class MainPageController {

    private final MainPageService mainPageService;

    public MainPageController(MainPageService mainPageService) {
        this.mainPageService = mainPageService;
    }

    @GetMapping("/carousel/list")
    public List<CarouselResponseDto> getCarousel() {
        return mainPageService.getCarousel();
    }

//    @GetMapping("/main/problem/try")
//    public List<RecentTryDto> getRecentTry(){
//        return mainPageService.getRecentTry();
//    }

}
