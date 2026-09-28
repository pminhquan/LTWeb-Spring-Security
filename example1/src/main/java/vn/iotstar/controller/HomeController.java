package vn.iotstar.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.iotstar.mapper.UserMapper;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;

@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public HomeController(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @GetMapping({"/", "/home"})
    public String home(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            userRepository.findByEmailWithRole(userDetails.getUsername())
                    .ifPresent(user -> model.addAttribute("user", userMapper.toDto(user)));
        }
        return "home";
    }
}
