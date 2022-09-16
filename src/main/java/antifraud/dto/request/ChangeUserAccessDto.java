package antifraud.dto.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ChangeUserAccessDto {
    @NotEmpty
    private String username;
    private String operation;
}
