package mathrone.backend.repository;


import mathrone.backend.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {


    Optional<Subscription> findByTid(String tid);
}
