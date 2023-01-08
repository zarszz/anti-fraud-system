package antifraud.persistence.repository;

import antifraud.persistence.model.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

    List<TransactionHistory> findByDateBetweenOrderByDateDesc(LocalDateTime startDate, LocalDateTime endDate);

    List<TransactionHistory> findByNumberOrderByIdAsc(String number);

    List<TransactionHistory> findAllByOrderByIdAsc();
}