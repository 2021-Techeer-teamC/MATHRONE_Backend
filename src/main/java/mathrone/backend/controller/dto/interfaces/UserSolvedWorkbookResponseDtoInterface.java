package mathrone.backend.controller.dto.interfaces;

public interface UserSolvedWorkbookResponseDtoInterface {

    String getWorkbookId();
    int getTotalProblemCount();
    int getSolvedProblemCount();

}