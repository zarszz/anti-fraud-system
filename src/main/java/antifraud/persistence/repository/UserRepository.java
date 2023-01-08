package antifraud.persistence.repository;

import antifraud.persistence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE lower(u.username) = lower(?1)")
    Optional<User> findByUsernameIgnoreCase(String username);
    void deleteByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);
    Optional<User> findByIsFirstUserTrue();

    List<User> findAllByOrderByIdDesc();
}
