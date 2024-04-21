package mathrone.backend.controller;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.RankDto;
import mathrone.backend.service.RankService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rank")
@RequiredArgsConstructor
public class RankingController {

    private final RankService rankService;

    @GetMapping("/total-rank")
    @ApiOperation(value = "상위 랭킹 정보 조회", notes = "모든 사용자의 랭킹 정보를 조회")
    public List<RankDto> getRank() {
        return rankService.getAllRank();
    }

    @GetMapping("/my-rank")
    @ApiOperation(value = "나의 랭킹 정보 조회", notes = "access token을 통해 사용자를 구분하여 랭킹 정보를 조회")
    public RankDto getMyRank(HttpServletRequest request) {
        return rankService.getMyRank(request);
    }
}
