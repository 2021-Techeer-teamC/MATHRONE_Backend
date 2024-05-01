package mathrone.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import mathrone.backend.domain.Problem;
import mathrone.backend.domain.ProblemTry;
import mathrone.backend.domain.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemTryRepository extends JpaRepository<ProblemTry, Long> {

    Optional<ProblemTry> findAllByProblemAndUser(Problem problem, UserInfo userInfo);

    Set<ProblemTry> findAllByUserAndIscorrectOrderByProblem(UserInfo userInfo, boolean correct);

    Set<ProblemTry> findAllByUserOrderByProblem(UserInfo userInfo);

    Optional<List<ProblemTry>> findProblemTryByUserAndIscorrect(UserInfo userInfo,
        Boolean iscorrect);
}
