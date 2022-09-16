package antifraud.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class DeleteSuspiciousDataResponseDto {
    @JsonIgnore
    private String context;

    @JsonIgnore
    private String value;

    @Getter(AccessLevel.NONE)
    private String status;

    public DeleteSuspiciousDataResponseDto(String ctx, String value) {
        this.context = ctx;
        this.value = value;
    }

    public String getStatus() {
        return context + " " + value + " successfully removed!";
    }
}
