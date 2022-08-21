package mathrone.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserProblemTryDTO {

    private String problemId;

    private Integer problemNum;

    private String chapterId;

    private String workbookId;

    private Integer levelOfDiff;

    private boolean iscorrect;

}
