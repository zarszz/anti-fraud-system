package antifraud.persistence.repository;

import antifraud.persistence.model.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

    @Query("SELECT t FROM TransactionHistory t WHERE t.date >= ?1 AND t.date <= ?2 ORDER BY t.date DESC")
    List<TransactionHistory> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<TransactionHistory> findByNumberOrderByIdAsc(String number);

    List<TransactionHistory> findAllByOrderByIdAsc();
}