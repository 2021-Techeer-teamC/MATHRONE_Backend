package mathrone.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.ChangeAccountIdDto;
import mathrone.backend.controller.dto.OauthDTO.GoogleIDToken;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoIDToken;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoTokenResponseDTO;
import mathrone.backend.controller.dto.OauthDTO.RequestCodeDTO;
import mathrone.backend.controller.dto.OauthDTO.ResponseTokenDTO;
import mathrone.backend.controller.dto.TokenDto;
import mathrone.backend.controller.dto.UserRequestDto;
import mathrone.backend.controller.dto.UserResponseDto;
import mathrone.backend.controller.dto.UserSignUpDto;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.service.AuthService;
import mathrone.backend.service.MailService;
import mathrone.backend.service.SnsLoginService;
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

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final SnsLoginService snsLoginService;
    private final MailService mailService;

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

    @PostMapping("/findId")
    public void findId(@RequestParam String email){ // 이메일 입력
        authService.findId(email);
    }

    @PostMapping("/findPw")
    public void findPw(@RequestParam String accountId, String email){
        authService.findPw(accountId, email);
    }

    @PostMapping("/change/password")
    public void changePw(HttpServletRequest request,
            @RequestParam String newPassword){
        authService.changePw(request, newPassword);
    }
}
