package mathrone.backend.controller.dto;

import lombok.Getter;

@Getter
public class UserEvaluateLevelRequestDto {

    private String workbookId;
    private int level;
}
