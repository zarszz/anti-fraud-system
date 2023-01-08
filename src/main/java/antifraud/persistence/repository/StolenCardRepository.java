package antifraud.persistence.repository;

import antifraud.persistence.model.StolenCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StolenCardRepository extends JpaRepository<StolenCard, Long> {
    Optional<StolenCard> findByNumber(String number);

    List<StolenCard> findAllByOrderByIdAsc();
}
