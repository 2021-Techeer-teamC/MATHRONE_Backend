package mathrone.backend.controller.dto;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserTriedProblemForGraphResponseDto {
    Map<String, Map<Integer, List<String>>> triedWorkbook = new TreeMap<>();

}
