package mathrone.backend.controller;

import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.*;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.domain.token.RefreshToken;
import mathrone.backend.service.AuthService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final AuthService authService;

    @GetMapping("/delUser")
    public ResponseEntity<Void> deleteUser(@RequestParam String email){
        authService.deleteUser(email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/allUser")
    public ResponseEntity<List<UserInfo>> allUser() {
        return ResponseEntity.ok(authService.allUser());
    }

    @GetMapping("/getRefreshList")
    public ResponseEntity<List<RefreshToken>> getRefreshList(){
        return ResponseEntity.ok(authService.getRefreshList());
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenDto> login (@RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.ok(authService.login(userRequestDto));
    }

    @PostMapping(value = "/logout", headers = {"Content-type=application/json"})
    public ResponseEntity<Void> logout( @RequestHeader String accessToken,
        @RequestHeader String refreshToken
    ){
        TokenRequestDto tokenRequestDto = new TokenRequestDto(accessToken,refreshToken);
        authService.logout(tokenRequestDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/signup", headers = {"Content-type=application/json"})
    public ResponseEntity<UserResponseDto> signUp(@RequestBody UserSignUpDto userSignUpDto){
        return ResponseEntity.ok(authService.signup(userSignUpDto));
    }

    @PostMapping(value = "/reissue")
    public ResponseEntity<TokenDto> reissue (@RequestHeader String accessToken,
        @RequestHeader String refreshToken){
        TokenRequestDto tokenRequestDto = new TokenRequestDto(accessToken,refreshToken);
        return ResponseEntity.ok(authService.reissue(tokenRequestDto));
    }

}
