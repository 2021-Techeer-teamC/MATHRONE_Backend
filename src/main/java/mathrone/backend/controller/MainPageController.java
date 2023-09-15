package mathrone.backend.controller;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.CarouselResponseDto;
import mathrone.backend.service.MainPageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/main")
public class MainPageController {

    private final MainPageService mainPageService;

    @GetMapping("/carousel/list")
    @ApiOperation(value = "메인 페이지 workbook 리스트 반환", notes = "메인 페이지에 보여줄 workbook 정보(image, title 등) 가져옴")
    public List<CarouselResponseDto> getCarousel() {
        return mainPageService.getCarousel();
    }

}
