package mathrone.backend.config.security;

import lombok.RequiredArgsConstructor;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.error.exception.ErrorCode;
import mathrone.backend.error.exception.CustomException;
import mathrone.backend.repository.UserInfoRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/*
    Repository를 통해 database로부터 필요한 user 정보를 가져오는 service
*/
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserInfoRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String nickname) throws UsernameNotFoundException {
        UserInfo isExist = userRepository.findByNickname(nickname).orElseThrow(() ->
            new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        return User.builder()
            .username(String.valueOf(isExist.getUserId()))
            .password(isExist.getPassword())
            .roles("USER")
            .build();
    }
}
