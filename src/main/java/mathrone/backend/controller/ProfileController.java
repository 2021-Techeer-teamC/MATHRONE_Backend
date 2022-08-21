package mathrone.backend.controller;

import mathrone.backend.domain.UserProfile;
import mathrone.backend.service.AuthService;
import mathrone.backend.service.ProfileService;
import org.springframework.web.bind.annotation.RequestHeader;
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
    public UserProfile getProfile(@RequestHeader String accessToken
    ) {
        return profileService.getProfile(authService.getUserIdFromAT(accessToken));
    }


}
