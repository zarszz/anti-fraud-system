package antifraud.dto.response;

import antifraud.persistence.model.Role;
import antifraud.persistence.model.User;
import lombok.Data;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Data
public class UserResponseDto {
    private long id;
    private String name;
    private String username;
    private String role;

    public static UserResponseDto fromEntity(User user) {
        var responses = new UserResponseDto();
        responses.setId(user.getId());
        responses.setName(user.getName());
        responses.setUsername(user.getUsername());
        responses.setRole(new ArrayList<>(user.getRoles()).get(0).getCode());
        return responses;
    }
}
