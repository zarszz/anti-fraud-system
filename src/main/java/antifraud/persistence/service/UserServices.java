package antifraud.persistence.service;

import antifraud.controller.exception.InvalidRoleException;
import antifraud.controller.exception.RoleAttachedException;
import antifraud.controller.exception.DataExistException;
import antifraud.dto.request.ChangeRoleDto;
import antifraud.dto.request.ChangeUserAccessDto;
import antifraud.persistence.model.User;
import antifraud.persistence.repository.RoleRepository;
import antifraud.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserServices implements UserDetailsService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServices(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsernameIgnoreCase(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        return user.get();
    }

    public User save(User user) {
        if (existByUsername(user.getUsername())) {
            throw new DataExistException("Usernmae already exist");
        }
        var roles = roleRepository.findByCodeIn(new String[]{"MERCHANT"});
        User persistedUser = null;

        var firstUserOptional = userRepository.findByIsFirstUserTrue();
        if (firstUserOptional.isEmpty()) {
            roles = roleRepository.findByCodeIn(new String[]{"ADMINISTRATOR"});
            user.setFirstUser(true);
            user.setAccountNonLocked(true);
            user.setRoles(new HashSet<>(roles));
            persistedUser = userRepository.saveAndFlush(user);
        } else {
            user.setFirstUser(false);
            user.setRoles(new HashSet<>(roles));
            persistedUser = userRepository.saveAndFlush(user);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(persistedUser);
    }

    public User updateRole(ChangeRoleDto changeRoleDto) {
        if (!changeRoleDto.getRole().equals("SUPPORT") && !changeRoleDto.getRole().equals("MERCHANT")) {
            throw new InvalidRoleException("Invalid role given !!");
        }

        var user = userRepository.findByUsernameIgnoreCase(changeRoleDto.getUsername());
        if (user.isEmpty()) {
            throw new NoSuchElementException("User not found");
        }

        var isRoleAttached = user.get().getRoles().stream().anyMatch(role -> role.getCode().equals(changeRoleDto.getRole()));
        if (isRoleAttached) {
            throw new RoleAttachedException("Role already attached to user");
        }

        var roles = roleRepository.findByCodeIn(new String[]{changeRoleDto.getRole()});
        if (roles.isEmpty()) {
            throw new NoSuchElementException("Role not found");
        }
        user.get().setRoles(new HashSet<>(roles));
        return userRepository.save(user.get());
    }

    @Transactional
    public void delete(String username) {
        if (!existByUsername(username)) {
            throw new NoSuchElementException("User not found");
        }
        userRepository.deleteByUsernameIgnoreCase(username);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public boolean existByUsername(String username) {
        return userRepository.existsByUsernameIgnoreCase(username);
    }

    public User changeAccess(ChangeUserAccessDto changeUserAccessDto) {
        var userOptional = userRepository.findByUsernameIgnoreCase(changeUserAccessDto.getUsername());
        if (userOptional.isEmpty()) {
            throw new NoSuchElementException("User not found");
        }
        var user = userOptional.get();
        if (changeUserAccessDto.getOperation().equalsIgnoreCase("LOCK")) {
            var isAdmin = user.getRoles().stream().anyMatch(role -> role.getCode().equals("ADMINISTRATOR"));
            if (isAdmin) {
                throw new InvalidRoleException("Can't lock admin user");
            }
            user.setAccountNonLocked(false);
        }

        if (changeUserAccessDto.getOperation().equalsIgnoreCase("UNLOCK")) {
            user.setAccountNonLocked(true);
        }

        return userRepository.saveAndFlush(user);
    }
}