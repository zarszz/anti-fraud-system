package antifraud.dto.response;

import antifraud.constant.TransactionResult;
import antifraud.persistence.model.TransactionHistory;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class TrxFeedbackResponseDto {
    private long transactionId;
    private double amount;
    private String ip;
    private String number;
    private String region;
    private LocalDateTime date;
    private String result;
    private String feedback;

    public static TrxFeedbackResponseDto fromEntity(TransactionHistory transactionHistory) {
        var response = new TrxFeedbackResponseDto();
        response.setTransactionId(transactionHistory.getId());
        response.setFeedback(
                transactionHistory.getFeedback().equals(TransactionResult.EMPTY)
                ? "" : transactionHistory.getFeedback().name()
        );
        response.setIp(transactionHistory.getIp());
        response.setDate(transactionHistory.getDate());
        response.setNumber(transactionHistory.getNumber());
        response.setAmount(transactionHistory.getAmount());
        response.setRegion(transactionHistory.getRegion());
        response.setResult(transactionHistory.getTransactionResult().name());
        return response;
    }
}
