package mathrone.backend.repository;

import java.util.Optional;
import mathrone.backend.domain.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {

    UserInfo findByUserId(int userId);

    Optional<UserInfo> findByAccountId(String accountId);

    UserInfo findByEmailAndResType(String email, String resType);

    UserInfo findByEmail(String email);

    // user_id를 통해서 user_nickname 조회
    @Query(value = "SELECT COUNT(*) FROM problem_try WHERE user_id=:userId GROUP BY user_id", nativeQuery = true)
    Long getTryByUserID(int userId);


    boolean existsUserInfoByAccountId(String accountId);

    boolean existsByEmailAndResType(String email, String resType);

    boolean existsByUserId(int userId);

    boolean existsByAccountId(String accountId);

    void deleteByAccountIdAndResType(String accountId, String resType);


}
