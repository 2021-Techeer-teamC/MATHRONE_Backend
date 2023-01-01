package mathrone.backend.controller;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.*;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoAuthResponseDTO;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoIDToken;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoTokenRequestDTO;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoTokenResponseDTO;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.domain.token.RefreshToken;
import mathrone.backend.service.AuthService;
import mathrone.backend.service.SnsLoginService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import mathrone.backend.controller.dto.OauthDTO.*;
import org.springframework.web.client.RestTemplate;

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
    public ResponseEntity<TokenDto> login(@RequestBody UserRequestDto userRequestDto) {
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
    public ResponseEntity<TokenDto> reissue(HttpServletRequest request, @RequestHeader String refreshToken) {
        return ResponseEntity.ok(authService.reissue(request, refreshToken));
    }


    @PostMapping(value = "/oauth/callback/google", headers = {"Content-type=application/json"})
    public ResponseEntity<TokenDto> moveGoogleInitUrl(@RequestBody RequestCodeDTO requestCodeDto) throws Exception {

        ResponseEntity<ResponseTokenDTO> res = snsLoginService.getToken(requestCodeDto.getCode());

        ResponseEntity<GoogleIDToken>res2 = snsLoginService.getGoogleIDToken(res);

        return ResponseEntity.ok(authService.googleLogin(res2));
    }


    @PostMapping(value = "/oauth/callback/kakao", headers = {"Content-type=application/json"})
    public ResponseEntity<TokenDto> moveKakaoInitUrl(@RequestBody RequestCodeDTO requestCodeDto) throws Exception {

        ResponseEntity<KakaoTokenResponseDTO> res = snsLoginService.getKakaoToken(requestCodeDto.getCode());
        ResponseEntity<KakaoIDToken> idInfo = snsLoginService.decodeIdToken(res.getBody().getId_token());

        return ResponseEntity.ok(authService.kakaoLogin(res,idInfo));
    }




//    @PostMapping(value = "/oauth/kakao/logout", headers = {"Content-type=application/json"})
//    public ResponseEntity<Void> kakaoLogout(HttpServletRequest request) {
//        authService.kakaoLogout(request);
//        return ResponseEntity.ok().build();
//    }





}
