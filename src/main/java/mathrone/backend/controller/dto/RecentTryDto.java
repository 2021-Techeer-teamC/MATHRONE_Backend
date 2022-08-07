package mathrone.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentTryDto {

    private String problemId;
    private String problemNum;
    private String workbookTitle;
    private int level;
    private String subject;
    private String chapter;
}
