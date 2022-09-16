package antifraud.dto.response;

import lombok.Data;

@Data
public class DeleteUserResponseDto {
    private String username;
    private String status;
}
