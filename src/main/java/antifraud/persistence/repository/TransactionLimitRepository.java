package antifraud.persistence.repository;

import antifraud.constant.TransactionLimitType;
import antifraud.persistence.model.TransactionLimit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionLimitRepository extends JpaRepository<TransactionLimit, Long> {
    @Query("SELECT t FROM TransactionLimit t WHERE t.transactionLimitType = ?1 ORDER BY t.id DESC")
    List<TransactionLimit> findTopByOrderByIdDescWhereTransactionLimitType(TransactionLimitType transactionLimitType);
}