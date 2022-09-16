package antifraud.dto.request;

import antifraud.persistence.model.User;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class SignupUserDto {
    @NotEmpty
    private String name;

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    public static User toEntity(SignupUserDto signupUserDto) {
        var user = new User();
        user.setName(signupUserDto.getName());
        user.setUsername(signupUserDto.getUsername());
        user.setPassword(signupUserDto.getPassword());
        return user;
    }
}
