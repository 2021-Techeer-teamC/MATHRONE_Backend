package mathrone.backend.controller;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoIDToken;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoTokenResponseDTO;
import mathrone.backend.controller.dto.OauthDTO.RequestCodeDTO;
import mathrone.backend.controller.dto.TokenDto;
import mathrone.backend.controller.dto.UserFailedTriedProblemsOfChapterDto;
import mathrone.backend.controller.dto.UserProblemTryDto;
import mathrone.backend.controller.dto.UserFailedTriedWorkbookResponseDto;
import mathrone.backend.domain.UserProfile;
import mathrone.backend.service.AuthService;
import mathrone.backend.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final AuthService authService;

    public ProfileController(ProfileService profileService, AuthService authService) {
        this.profileService = profileService;
        this.authService = authService;
    }


    @RequestMapping(value = "/myprofile", method = RequestMethod.GET)
    public UserProfile getProfile(HttpServletRequest request) {
        return profileService.getProfile(authService.getUserIdFromAT(request));
    }

    @ApiOperation(value = "유저가 시도한 문제 반환")
    @GetMapping("/problme")
    public ResponseEntity<List<UserProblemTryDto>> getTryProblem(HttpServletRequest request) {
        return ResponseEntity.ok(profileService.getTryProblem(request));
    }

    @ApiOperation(value = "유저가 시도한 문제 중 틀린 문제를 가져오는 그래프")
    @GetMapping("/problem/analysis")
    public ResponseEntity<UserFailedTriedWorkbookResponseDto> getTriedProblemForGraph(
        HttpServletRequest request) {
        return ResponseEntity.ok(profileService.getTriedProblemForGraph(request));
    }

    @ApiOperation(value = "유저가 시도한 문제 중 특정 단원의 틀린 문제 정보 반환")
    @GetMapping("/problem/analysis/list")
    public ResponseEntity<UserFailedTriedProblemsOfChapterDto> getUserFailedProblemsOfChapterOfWorkbook(
        HttpServletRequest request, @RequestParam String workbookId, @RequestParam String chapterId) {
        return ResponseEntity.ok(profileService.getUserFailedProblemsOfChapterOfWorkbook(request, workbookId, chapterId));
    }

    //kakaopay
    @PostMapping(value = "/premium/kakaopay", headers = {"Content-type=application/json"})
    public ResponseEntity<TokenDto> moveKakaoInitUrl(){

        //


        return ;
    }

}
