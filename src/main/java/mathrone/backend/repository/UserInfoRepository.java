package mathrone.backend.repository;

import java.util.Optional;
import mathrone.backend.domain.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {

    UserInfo findByUserId(Integer userId);

    Optional<UserInfo> findByAccountId(String accountId);

    // user_id를 통해서 user_nickname 조회
    @Query(value = "SELECT COUNT(*) FROM problem_try WHERE user_id=:userId GROUP BY user_id", nativeQuery = true)
    Long getTryByUserID(int userId);

    boolean existsUserInfoByAccountId(String accountId);

    boolean existsByEmailAndResType(String email, String resType);

    boolean existsByAccountId(String accountId);

    Optional<Void> deleteByAccountIdAndResType(String accountId, String resType);


}
