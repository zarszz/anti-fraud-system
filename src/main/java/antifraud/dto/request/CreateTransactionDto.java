package antifraud.dto.request;

import lombok.Data;
import org.hibernate.validator.constraints.LuhnCheck;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class CreateTransactionDto {
    @Min(1L)
    private double amount;

    @NotEmpty
    @Pattern(regexp = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")
    private String ip;

    @NotEmpty
    @LuhnCheck
    private String number;

    private String region;

    @DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm:ss")
    private LocalDateTime date;

    // from entity
    public static CreateTransactionDto fromEntity(antifraud.persistence.model.TransactionHistory entity) {
        var dto = new CreateTransactionDto();
        dto.setAmount(entity.getAmount());
        dto.setIp(entity.getIp());
        dto.setNumber(entity.getNumber());
        dto.setRegion(entity.getRegion());
        dto.setDate(entity.getDate());
        return dto;
    }
}
