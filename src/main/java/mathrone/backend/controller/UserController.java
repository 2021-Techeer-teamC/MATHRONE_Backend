package mathrone.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.ApiOperation;

import java.net.URI;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.*;
import mathrone.backend.controller.dto.OauthDTO.GoogleIDToken;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoIDToken;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoOAuthLoginUtils;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoTokenResponseDTO;
import mathrone.backend.controller.dto.OauthDTO.RequestCodeDTO;
import mathrone.backend.controller.dto.OauthDTO.ResponseTokenDTO;
import mathrone.backend.domain.ReactiveUserDto;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.service.AuthService;
import mathrone.backend.service.SnsLoginService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;

import javax.validation.Valid;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final SnsLoginService snsLoginService;


    @GetMapping("/delUser")
    @ApiOperation(value = "사용자 삭제", notes = "DB에 존재하는 사용자를 삭제")
    public ResponseEntity<Void> deleteUser(@RequestParam String accountId, String resType) {
        authService.deleteUser(accountId, resType);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/allUser")
    @ApiOperation(value = "모든 사용자 조회", notes = "DB에 존재하는 모든 사용자를 리스트로 반환")
    public ResponseEntity<List<UserInfo>> allUser() {
        return ResponseEntity.ok(authService.allUser());
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Mathrone 로그인", notes = "id와 password를 받아 로그인 수행")
    public ResponseEntity<TokenDto> login(@RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.ok(authService.login(userRequestDto));
    }

    @PostMapping(value = "/email-verify")
    @ApiOperation(value = "이메일 인증 ", notes = "이메일 인증기능")
    public ResponseEntity<EmailVerifyDto> emailVerify(@RequestBody EmailVerifyRequest emailVerifyRequest) {
        return ResponseEntity.ok(authService.emailVerify(emailVerifyRequest));
    }

    @PostMapping(value = "/logout", headers = {"Content-type=application/json"})
    @ApiOperation(value = "Mathrone 로그아웃", notes = "Mathrone 로그아웃을 진행함과 동시에, access token 처리")
    public ResponseEntity<Void> logout(HttpServletRequest request
    ) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/kakao/logout", headers = {"Content-type=application/json"})
    @ApiOperation(value = "카카오 계정 로그아웃")
    public ResponseEntity<Void> logoutWithKakao(HttpServletRequest request
    ) {
        authService.logoutWithKakao(request);
        return ResponseEntity.ok().build();
    }


    @PostMapping(value = "/google/logout", headers = {"Content-type=application/json"})
    @ApiOperation(value = "구글 계정 로그아웃")
    public ResponseEntity<Void> logoutWithGoogle(HttpServletRequest request
    ) {
        authService.logoutWithGoogle(request);
        return ResponseEntity.ok().build();
    }


    @GetMapping(value = "/check/accountId", headers = {"Content-type=application/json"})
    @ApiOperation(value = "유저의 id 검증", notes = "회원가입 시 아이디 중복 확인")
    public ResponseEntity<Void> validateUserAccountId(@RequestParam String userAccountId) {
        authService.validateUserAccountId(userAccountId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/signup", headers = {"Content-type=application/json"})
    @ApiOperation(value = "Mathrone 회원 가입")
    public ResponseEntity<UserResponseDto> signUp(@RequestBody UserSignUpDto userSignUpDto) {
        return ResponseEntity.ok(authService.signup(userSignUpDto));
    }

    @PostMapping(value = "/reissue")
    @ApiOperation(value = "Mathrone 토큰 재발급", notes = "access token 만료에 따른 재발급 진행")
    public ResponseEntity<TokenDto> reissue(HttpServletRequest request,
        @RequestHeader String refreshToken) {
        return ResponseEntity.ok(authService.reissue(request, refreshToken));
    }

    @PostMapping(value = "/kakao/reissue")
    @ApiOperation(value = "카카오 토큰 재발급", notes = "카카오 access token 만료에 따른 재발급 진행")
    public ResponseEntity<TokenDto> kakaoReissue(HttpServletRequest request, String userId)
        throws JsonProcessingException {

        ResponseEntity<KakaoTokenResponseDTO> reissue = snsLoginService.kakaoReissue(userId);
        ResponseEntity<KakaoIDToken> idInfo = snsLoginService.decodeIdToken(
            reissue.getBody().getId_token());

        return ResponseEntity.ok(authService.kakaoReissue(request, reissue, idInfo));
    }


    @PostMapping(value = "/google/reissue")
    @ApiOperation(value = "구글 토큰 재발급", notes = "구글 access token 만료에 따른 재발급 진행")
    public ResponseEntity<TokenDto> googleReissue(HttpServletRequest request, String userId)
        throws Exception {

        ResponseEntity<ResponseTokenDTO> reissue = snsLoginService.googleReissue(userId);
        ResponseEntity<GoogleIDToken> idInfo = snsLoginService.getGoogleIDToken(reissue);

        return ResponseEntity.ok(authService.googleReissue(request, reissue, idInfo));
    }

    @PostMapping(value = "/oauth/callback/google", headers = {"Content-type=application/json"})
    @ApiOperation(value = "구글 회원가입 진행", notes = "구글 로그인 시 구글 계정으로 회원가입이 되어있지 않은 경우, 회원가입 진행")
    public ResponseEntity<TokenDto> moveGoogleInitUrl(@RequestBody RequestCodeDTO requestCodeDto)
        throws Exception {

        //get token from code
        ResponseEntity<ResponseTokenDTO> res = snsLoginService.getToken(requestCodeDto.getCode());

        //get id token from accesstoken
        ResponseEntity<GoogleIDToken> res2 = snsLoginService.getGoogleIDToken(res);

        //mathrone signup with google id token
        return ResponseEntity.ok(authService.googleLogin(res2, res));
    }

    @PutMapping(value = "/accountId", headers = {"Content-type=application/json"})
    @ApiOperation(value = "accountId 업데이트")
    public ResponseEntity<Void> updateAccountId(@RequestBody ChangeAccountIdDto accountId,
        HttpServletRequest request) {

        //accessToken을 통해 userID알아내기 (primary key)
        UserInfo user = authService.findUserFromRequest(request);

        //accountID update
        authService.updateAccountId(accountId.getAccountId(), user);

        return ResponseEntity.ok().build();

    }


    @PostMapping(value = "/oauth/callback/kakao", headers = {"Content-type=application/json"})
    @ApiOperation(value = "카카오 로그인", notes = "카카오 계정으로 회원가입이 되어있지 않은 경우, 회원가입도 같이 진행")
    public ResponseEntity<TokenDto> moveKakaoInitUrl(@RequestBody RequestCodeDTO requestCodeDto)
        throws Exception {
        ResponseEntity<KakaoTokenResponseDTO> res = snsLoginService.getKakaoToken(
            requestCodeDto.getCode());
        ResponseEntity<KakaoIDToken> idInfo = snsLoginService.decodeIdToken(
            res.getBody().getId_token());

        return ResponseEntity.ok(authService.kakaoLogin(res, idInfo));
    }




    @GetMapping(value = "/kakao/login-request", headers = {"Content-type=application/json"})
    @ApiOperation(value = "카카오 로그인 1단계 : 로그인 요청", notes = "카카오 로그인 페이지로 리다이렉트")
    public ResponseEntity requestKakaoLogin(){
        return snsLoginService.redirectKakaoLoginPage();
    }



    @GetMapping(value = "/kakao/logout-request", headers = {"Content-type=application/json"})
    @ApiOperation(value = "카카오 로그아웃 2단계 : 카카오서비스에서 로그아웃", notes = "카카오 로그아웃 페이지로 리다이렉트")
    public ResponseEntity requestKakaoLogoutFromKakao(){
        return snsLoginService.redirectKakaoLogoutPage();
    }


    @GetMapping(value = "/google/login-request", headers = {"Content-type=application/json"})
    @ApiOperation(value = "구글 로그인 1단계 : 로그인 요청", notes = "구글 로그인 페이지로 리다이렉트")
    public ResponseEntity requestGoogleLogin(){
        return snsLoginService.redirectGoogleLoginPage();
    }


//    @GetMapping(value = "/google/logout-request", headers = {"Content-type=application/json"})
//    @ApiOperation(value = "구글 로그아웃 2단계 : 구글에서 로그아웃", notes = "구글 로그아웃 페이지로 리다이렉트")
//    public ResponseEntity requestGoogleLogoutFromGoogle(){
//        return snsLoginService.redirectGoogleLogoutPage();
//    }

    @PostMapping("/find/id")
    @ApiOperation(value = "아이디 찾기", notes = "입력받은 이메일에 대한 아이디를 찾아 이메일 발송")
    public void findId(@RequestBody @Valid FindDto request) {
        authService.findId(request);
    }

    @PostMapping("/find/password")
    @ApiOperation(value = "비밀번호 찾기", notes = "입력받은 아이디에 대한 임시 패스워드를 발급하여 이메일 발송")
    public void findPw(@RequestBody @Valid FindDto request) {
        authService.findPw(request);
    }

    @PatchMapping("/password")
    @ApiOperation(value = "비밀번호 변경", notes = "현재 유저의 패스워드 수정")
    public void changePw(HttpServletRequest request,
        @RequestBody @Valid ChangePasswordDto newPassword) {
        authService.changePw(request, newPassword);
    }


    @PatchMapping("/deactivate")
    @ApiOperation(value = "회원 탈퇴", notes = "해당 유저의 activate상태를 비활성하고 토큰을 뺐음 ")
    public void deactivateUser(HttpServletRequest request) {

        authService.deactiveUser(request);
    }

    @PostMapping("/activate-code")
    @ApiOperation(value = "회원 복구코드 발급 - 아이디, 비밀번호 필요", notes = "유저의 복구코드를 발급받음")
    public ResponseEntity<ReactiveUserDto> activateUser(
            @RequestBody UserRequestDto userRequestDto
    ) {
        return ResponseEntity.ok(authService.getReactivateCode(userRequestDto));
    }

    @PatchMapping("/activate")
    @ApiOperation(value = "회원 복구코드 발급 - 아이디, 비밀번호 필요", notes = "유저의 복구코드를 발급받음")
    public void activateUser(
            @RequestBody ReactiveUserDto reactiveUserDto
    ) {
        authService.reactiveUser(reactiveUserDto);
    }



}
