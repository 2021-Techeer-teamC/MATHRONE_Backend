package mathrone.backend.controller;

import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.UserFailedTriedProblemsOfChapterDto;
import mathrone.backend.controller.dto.UserFailedTriedWorkbookResponseDto;
import mathrone.backend.domain.UserProfile;
import mathrone.backend.service.AuthService;
import mathrone.backend.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final AuthService authService;


    @RequestMapping(value = "/my-profile", method = RequestMethod.GET)
    @ApiOperation(value = "유저의 프로필 정보 조회")
    public UserProfile getProfile(HttpServletRequest request) {
        return profileService.getProfile(authService.getUserIdFromAT(request));
    }

    @GetMapping("/problem/analysis")
    @ApiOperation(value = "유저가 시도한 문제 중 틀린 문제를 가져오는 그래프", notes = "프리미엄 유저가 푼 문제에 대한 분석 그래프 제공")
    public ResponseEntity<UserFailedTriedWorkbookResponseDto> getTriedProblemForGraph(
        HttpServletRequest request) {
        return ResponseEntity.ok(profileService.getTriedProblemForGraph(request));
    }

    @GetMapping("/problem/analysis/list")
    @ApiOperation(value = "유저가 시도한 문제 중 특정 단원의 틀린 문제 정보 반환", notes = "프리미엄 유저가 푼 문제에 대한 분석 그래프 제공")
    public ResponseEntity<UserFailedTriedProblemsOfChapterDto> getUserFailedProblemsOfChapterOfWorkbook(
        HttpServletRequest request, @RequestParam String workbookId,
        @RequestParam String chapterId) {
        return ResponseEntity.ok(
            profileService.getUserFailedProblemsOfChapterOfWorkbook(request, workbookId,
                chapterId));
    }

}
