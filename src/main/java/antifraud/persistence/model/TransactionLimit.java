package antifraud.persistence.model;

import antifraud.constant.TransactionLimitType;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
public class TransactionLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "transaction_limit")
    private double limit;

    @Enumerated(EnumType.STRING)
    private TransactionLimitType transactionLimitType;

    @CreationTimestamp
    private LocalDateTime date;
}
