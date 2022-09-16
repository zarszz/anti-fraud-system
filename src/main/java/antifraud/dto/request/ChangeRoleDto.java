package antifraud.dto.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ChangeRoleDto {
    @NotEmpty
    private String username;

    @NotEmpty
    private String role;
}
