package mathrone.backend.repository;


import mathrone.backend.domain.Solution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolutionRepository extends JpaRepository<Solution, Long> {
    Solution findSolutionByProblemId(String problemId);
}
