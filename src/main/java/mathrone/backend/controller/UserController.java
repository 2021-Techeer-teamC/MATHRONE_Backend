package mathrone.backend.controller;

import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.*;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.domain.token.RefreshToken;
import mathrone.backend.service.AuthService;
import mathrone.backend.service.SnsLoginService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import mathrone.backend.controller.dto.OauthDTO.*;

import java.util.List;
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final SnsLoginService snsLoginService;

    @GetMapping("/delUser")
    public ResponseEntity<Void> deleteUser(@RequestParam String accountId, String resType) {
        authService.deleteUser(accountId, resType);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/allUser")
    public ResponseEntity<List<UserInfo>> allUser() {
        return ResponseEntity.ok(authService.allUser());
    }

    @GetMapping("/getRefreshList")
    public ResponseEntity<List<RefreshToken>> getRefreshList() {
        return ResponseEntity.ok(authService.getRefreshList());
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenDto> login(@RequestBody UserRequestDto userRequestDto) throws Exception{
        return ResponseEntity.ok(authService.login(userRequestDto));
    }

    @PostMapping(value = "/logout", headers = {"Content-type=application/json"})
    public ResponseEntity<Void> logout(HttpServletRequest request
    ) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/check/accountId", headers = {"Content-type=application/json"})
    public ResponseEntity<Void> validateUserAccountId(@RequestParam String userAccountId) {
        authService.validateUserAccountId(userAccountId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/signup", headers = {"Content-type=application/json"})
    public ResponseEntity<UserResponseDto> signUp(@RequestBody UserSignUpDto userSignUpDto) {
        return ResponseEntity.ok(authService.signup(userSignUpDto));
    }

    @PostMapping(value = "/reissue")
    public ResponseEntity<TokenDto> reissue(HttpServletRequest request,
        @RequestHeader String refreshToken) {
        return ResponseEntity.ok(authService.reissue(request, refreshToken));
    }


    //구글 로그인 (회원가입이 되지 않은 경우 회원가입 까지 해주기)
    @PostMapping(value = "/oauth/callback/google", headers = {"Content-type=application/json"})
    public ResponseEntity<TokenDto> moveGoogleInitUrl(@RequestBody RequestCodeDTO requestCodeDto, String accountID) throws Exception {

        //get token from code
        ResponseEntity<ResponseTokenDTO> res = snsLoginService.getToken(requestCodeDto.getCode());

        //get id token from accesstoken
        ResponseEntity<GoogleIDToken> res2 = snsLoginService.getGoogleIDToken(res);

        //mathrone signup with google id token
        return ResponseEntity.ok(authService.googleLogin(res2));
    }

    //accoutID update -> "PUT"으로 변경
    @PostMapping(value = "/accountID", headers = {"Content-type=application/json"})
    public ResponseEntity<Void> updateAccountId(HttpServletRequest request, String accountID){
        //accessToken을 통해 userID알아내기 (primary key)
        UserInfo user = authService.findUserFromRequest(request);

        //accountID update
        authService.updateAccountID(accountID, user);

        return ResponseEntity.ok().build();

    }


    //구글 로그인
//    @PostMapping(value = "/snslogin", headers = {"Content-type=application/json"})
//    public ResponseEntity<TokenDto> moveGoogleInitUrl(@RequestBody RequestCodeDTO requestCodeDto) throws Exception {
//
//        //get token from code
//        ResponseEntity<ResponseTokenDTO> res = snsLoginService.getToken(requestCodeDto.getCode());
//
//        //get id token from accesstoken
//        ResponseEntity<GoogleIDToken> res2 = snsLoginService.getGoogleIDToken(res);
//
//        //mathrone login with google id token
//        return ResponseEntity.ok(authService.googleLogin(res2));
//    }

}
