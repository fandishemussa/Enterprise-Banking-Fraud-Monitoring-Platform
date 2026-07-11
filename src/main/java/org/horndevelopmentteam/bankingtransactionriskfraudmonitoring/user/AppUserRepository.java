package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUserId(String userId);

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByRole(Role role);

    List<AppUser> findByStatusOrderByUsername(UserStatus status);
}
