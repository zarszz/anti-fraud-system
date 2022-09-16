package antifraud.dto.request;

import antifraud.constant.TransactionResult;
import antifraud.dto.request.validation.EnumValidator;
import lombok.Data;

@Data
public class AddTrxFeedbackDto {
    private long transactionId;

    @EnumValidator(enumClass = TransactionResult.class)
    private String feedback;
}
