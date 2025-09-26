package fsa.training.travelee.controller.page;

import fsa.training.travelee.entity.User;
import fsa.training.travelee.service.UserService;
import fsa.training.travelee.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final UserService userService;

    @GetMapping({"/","", "/home","/page/home"})
    public String showPageHome(Model model) {
        User currentUser = userService.getCurrentUser();

        if (currentUser != null) {
            model.addAttribute("fullName", currentUser.getFullName());
            model.addAttribute("loggedInUser", currentUser);
        }

        return "page/home";
    }

    @GetMapping("/oauth2/authorization/google")
    public String showGoogleLogin() {
        return "oauth2/authorization/google";
    }

    @GetMapping("/page/about")
    public String showPageAbout() {
        return "page/about";
    }


}
