package mathrone.backend.controller;

import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.ChangeProfileDto;
import mathrone.backend.controller.dto.UserFailedTriedProblemsOfChapterDto;
import mathrone.backend.controller.dto.UserFailedTriedWorkbookResponseDto;
import mathrone.backend.domain.UserProfile;
import mathrone.backend.service.AuthService;
import mathrone.backend.service.ProfileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping( "/edit")
    @ApiOperation(value = "유저 개인정보 변경", notes = "유저 프로필 이미지, 닉네임, 전화번호 변경 가능")
    public ResponseEntity changeProfile(@RequestPart ChangeProfileDto changeProfileDto,
        HttpServletRequest request) {
        return profileService.changeProfile(changeProfileDto, request);
    }

    @PostMapping("/img")
    @ApiOperation(value = "프로필 이미지 등록")
    public ResponseEntity changeImg(@RequestPart MultipartFile profileImg,
            HttpServletRequest request) throws IOException {
        return profileService.changeImg(profileImg, request);
    }
}
