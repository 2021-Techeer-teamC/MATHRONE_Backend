package mathrone.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Getter
public class AllRankDto {
    Long correct_count;
    String user_name;
    Long try_count;

}

//@AllArgsConstructor
//@Builder
//public class RankListDto{
//    AllRankDto[] rankList;
//}
