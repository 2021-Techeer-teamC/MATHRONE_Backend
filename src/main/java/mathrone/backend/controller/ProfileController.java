package mathrone.backend.controller;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import mathrone.backend.controller.dto.UserProblemTryDTO;
import mathrone.backend.domain.UserProfile;
import mathrone.backend.service.AuthService;
import mathrone.backend.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<List<UserProblemTryDTO>> getTryProblem(HttpServletRequest request) {
        return ResponseEntity.ok(profileService.getTryProblem(request));
    }


}
