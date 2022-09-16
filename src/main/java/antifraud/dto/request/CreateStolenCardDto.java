package antifraud.dto.request;

import lombok.Data;
import org.hibernate.validator.constraints.LuhnCheck;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class CreateStolenCardDto {
    @NotEmpty
    @LuhnCheck
    private String number;
}
