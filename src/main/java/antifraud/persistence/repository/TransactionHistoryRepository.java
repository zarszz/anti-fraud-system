package antifraud.persistence.repository;

import antifraud.persistence.model.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

    @Query("SELECT t FROM TransactionHistory t WHERE t.date >= ?1 AND t.date <= ?2")
    List<TransactionHistory> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<TransactionHistory> findByNumber(String number);
}