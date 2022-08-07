package mathrone.backend.controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mathrone.backend.service.RankService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rank")
public class RankingController {
    private final RankService rankService;

    public RankingController(RankService rankService) {
        this.rankService = rankService;
    }

    @GetMapping("/rankdata") // 상위 랭킹 정보를 가져옴
    public ArrayNode getRank(){
        return rankService.getAllRank();
    }

    @GetMapping("/myrankdata") // 나의 랭킹 정보를 가져오기
    public ObjectNode getMyRank(@RequestParam(value="userId", required = false) Integer userId){  // user_id(int)를 파라미터로 필요로 함
        return rankService.getMyRank(userId);
    }

    @PostMapping("/setdata") // 맞춘 문제에 개수 업데이트하기
    public void setRank(/*nickname*/){
        rankService.setRank(/*nickname*/);
    }
}
