package antifraud.controller;

import antifraud.dto.request.ChangeRoleDto;
import antifraud.dto.request.ChangeUserAccessDto;
import antifraud.dto.request.SignupUserDto;
import antifraud.dto.response.ChangeUserAccessResponseDto;
import antifraud.dto.response.DeleteUserResponseDto;
import antifraud.dto.response.UserResponseDto;
import antifraud.persistence.model.User;
import antifraud.persistence.service.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserServices userServices;

    @Autowired
    public AuthController(UserServices userServices) {
        this.userServices = userServices;
    }

    @PostMapping("/user")
    public ResponseEntity<?> register(@RequestBody @Valid SignupUserDto signupUserDto) {
        var user = userServices.save(SignupUserDto.toEntity(signupUserDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponseDto.fromEntity(user));
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(
            userServices.getAll()
                .stream()
                .sorted(Comparator.comparing(User::getId))
                .map(UserResponseDto::fromEntity)
                .collect(Collectors.toList())
        );
    }

    @PutMapping("/role")
    public ResponseEntity<?> updateRole(@RequestBody ChangeRoleDto changeRoleRequestDto) {
        var user = userServices.updateRole(changeRoleRequestDto);
        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    @PutMapping("/access")
    public ResponseEntity<?> changeAccess(@RequestBody ChangeUserAccessDto changeUserAccessDto) {
        userServices.changeAccess(changeUserAccessDto);
        var response = new ChangeUserAccessResponseDto();
        response.setStatus("User " + changeUserAccessDto.getUsername() + " " + changeUserAccessDto.getOperation().toLowerCase() + "ed!");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user/{username}")
    public ResponseEntity<?> delete(@PathVariable String username) {
        userServices.delete(username);
        var response = new DeleteUserResponseDto();
        response.setUsername(username);
        response.setStatus("Deleted successfully!");
        return ResponseEntity.ok(response);
    }
}
