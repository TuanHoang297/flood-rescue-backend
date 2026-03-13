package com.example.login.service;
import com.example.login.model.User;
import com.example.login.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;
@Service
public class UserService {
    public enum PasswordChangeResult {
        SUCCESS,
        USER_NOT_FOUND,
        OLD_PASSWORD_INCORRECT,
        NEW_PASSWORD_INVALID
    }

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Xac thuc nguoi dung: tim theo username, kiem tra password.
     * Tra ve Optional<User> neu hop le, empty neu sai.
     */
    public Optional<User> authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            return userOpt;
        }
        return Optional.empty();
    }

    /**
     * Doi mat khau cho user dang nhap.
     */
    public PasswordChangeResult changePassword(String username, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return PasswordChangeResult.USER_NOT_FOUND;
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(oldPassword)) {
            return PasswordChangeResult.OLD_PASSWORD_INCORRECT;
        }

        if (newPassword == null || newPassword.length() < 6) {
            return PasswordChangeResult.NEW_PASSWORD_INVALID;
        }

        user.setPassword(newPassword);
        userRepository.save(user);
        return PasswordChangeResult.SUCCESS;
    }
}
