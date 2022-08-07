package mathrone.backend.controller;

import mathrone.backend.controller.dto.TokenRequestDto;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.domain.UserProfile;
import mathrone.backend.service.AuthService;
import mathrone.backend.service.MainPageService;
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
    public UserProfile getProfile( @RequestHeader String accessToken,
                                   @RequestHeader String refreshToken
    ){
        return profileService.getProfile(authService.getUserIdFromAT(new TokenRequestDto(accessToken,refreshToken)));
    }


}
