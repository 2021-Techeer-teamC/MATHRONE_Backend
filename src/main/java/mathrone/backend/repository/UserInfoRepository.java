package mathrone.backend.repository;

import java.util.Optional;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.domain.enums.UserResType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {

    UserInfo findByUserId(int userId);

    Optional<UserInfo> findByNickname(String nickname);

    UserInfo findByEmailAndResType(String email, UserResType resType);

    Optional<UserInfo> findByEmail(String email);

    Optional<UserInfo> findByEmailAndNickname(String email, String nickname);

    // user_id를 통해서 user_nickname 조회
    @Query(value = "SELECT COUNT(*) FROM problem_try WHERE user_id=:userId GROUP BY user_id", nativeQuery = true)
    Long getTryByUserID(int userId);


    boolean existsUserInfoByNickname(String nickname);

    boolean existsByEmailAndResType(String email, UserResType resType);

    boolean existsByUserId(int userId);

    boolean existsByNickname(String nickname);

    void deleteByNicknameAndResType(String nickname, UserResType resType);

    UserInfo findByEmailAndResTypeAndActivateTrue(String email, String resType);

    UserInfo findByUserIdAndActivateTrue(int userId);
}
