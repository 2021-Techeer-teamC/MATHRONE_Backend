package mathrone.backend.repository;

import mathrone.backend.domain.UserFailedTriedWorkbookRedis;
import org.springframework.data.repository.CrudRepository;

public interface UserFailedTriedWorkbookRedisRepository extends
    CrudRepository<UserFailedTriedWorkbookRedis, String> {

}
