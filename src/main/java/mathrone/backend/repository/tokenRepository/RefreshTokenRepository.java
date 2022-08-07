package mathrone.backend.repository.tokenRepository;

import mathrone.backend.domain.token.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUserId(String userid);
    Optional<RefreshToken> deleteByUserId(String userId);
}
