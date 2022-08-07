package mathrone.backend.config.security;

import lombok.RequiredArgsConstructor;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.repository.UserInfoRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import mathrone.backend.config.CacheKey;
import org.springframework.cache.annotation.Cacheable;
import java.util.List;

/*
    Repository를 통해 database로부터 필요한 user 정보를 가져오는 service
*/
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserInfoRepository userRepository;

    @Override
//    @Cacheable(value = CacheKey.USER, key = "#email", unless = "#result == null")
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserInfo isExist = userRepository.findByEmail(email).orElseThrow( () ->
                new UsernameNotFoundException("유저를 찾을 수 없습니다. 이메일을 다시 확인해주세요."));

        return User.builder()
            .username(isExist.getId())
            .password(isExist.getPassword())
            .roles("USER")
            .build();
    }
}
