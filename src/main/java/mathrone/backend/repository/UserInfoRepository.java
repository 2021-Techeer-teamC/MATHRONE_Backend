package mathrone.backend.repository;

import java.util.Optional;
import mathrone.backend.domain.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {

    UserInfo findByUserId(Integer userId);


    UserInfo findById(String id);

    UserInfo findByEmailAndResType(String email, String resType);


    // user_id를 통해서 user_nickname 조회
    @Query(value = "SELECT COUNT(*) FROM problem_try WHERE user_id=:userId GROUP BY user_id", nativeQuery = true)
    Long getTryByUserID(int userId);



    boolean existsByEmailAndResType(String email, String resType);

    Optional<UserInfo> findByEmail(String email);//??? 왜
    boolean existsByEmail(String email);

    Optional<Void> deleteByEmail(String email);


}
