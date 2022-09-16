package antifraud.persistence.model;

import antifraud.constant.TransactionResult;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
public class TransactionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private double amount;
    private String ip;
    private String number;
    private String region;

    @Enumerated(EnumType.STRING)
    private TransactionResult transactionResult;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) DEFAULT 'EMPTY'")
    private TransactionResult feedback = TransactionResult.EMPTY;

    private LocalDateTime date;
}
