package mathrone.backend.repository;


import mathrone.backend.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {


    Optional<Subscription> findByTid(String tid);


    //구독이 (완료된것만) complete 상태이며, 가장 최신의 것(가장 최근 구독내역으로)
    @Query(value = "select * from subscription where user_id=:userId and status = 'COMPLETE' order by modified_date desc limit 1", nativeQuery = true)
    Optional<Subscription> checkLastSubscription(int userId);



    Optional<Subscription> findByUserId(int userId);
}
