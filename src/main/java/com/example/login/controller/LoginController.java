package com.example.login.controller;
import com.example.login.model.User;
import com.example.login.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {
    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    // Redirect trang chu "/" -> "/login"
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    // Hien thi trang login
    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        // Neu da dang nhap roi thi chuyen thang vao /home
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/home";
        }
        return "login";
    }
    // Xu ly form login (POST)
    @PostMapping("/login")
    public String handleLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        Optional<User> userOpt = userService.authenticate(username, password);
        if (userOpt.isPresent()) {
            // Luu thong tin user vao session
            session.setAttribute("loggedInUser", userOpt.get().getUsername());
            return "redirect:/home";
        } else {
            // Sai username hoac password
            model.addAttribute("error", "Ten dang nhap hoac mat khau khong dung!");
            model.addAttribute("username", username);
            return "login";
        }
    }

    // Trang home sau khi dang nhap thanh cong
    @GetMapping("/home")
    public String showHomePage(
            HttpSession session,
            Model model,
            @RequestParam(required = false) String passwordChanged) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            // Chua dang nhap -> quay lai trang login
            return "redirect:/login";
        }

        if ("1".equals(passwordChanged)) {
            model.addAttribute("success", "Doi mat khau thanh cong!");
        }

        model.addAttribute("username", loggedInUser);
        return "home";
    }

    @GetMapping("/change-password")
    public String showChangePasswordPage(HttpSession session) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        return "change-password";
    }

    @PostMapping("/change-password")
    public String handleChangePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            Model model) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mat khau moi va xac nhan mat khau khong khop.");
            return "change-password";
        }

        UserService.PasswordChangeResult result = userService.changePassword(loggedInUser, oldPassword, newPassword);
        if (result == UserService.PasswordChangeResult.SUCCESS) {
            return "redirect:/home?passwordChanged=1";
        }

        if (result == UserService.PasswordChangeResult.OLD_PASSWORD_INCORRECT) {
            model.addAttribute("error", "Mat khau hien tai khong dung.");
            return "change-password";
        }

        model.addAttribute("error", "Mat khau moi phai co it nhat 6 ky tu.");
        return "change-password";
    }

    // Dang xuat
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
