package antifraud.dto.response;

import antifraud.constant.TransactionResult;
import lombok.Data;

@Data
public class CreateTransactionResponseDto {
    private TransactionResult result;
    private String info;
}
