package com.kreuterkeule.MeatMessenger.controller;
import com.kreuterkeule.MeatMessenger.security.JwtUtils;
import com.kreuterkeule.MeatMessenger.services.UniqueTokenProviderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@CrossOrigin

public class HomeController {

    private JwtUtils jwtUtils;

    @Autowired
    public HomeController(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Autowired
    UniqueTokenProviderService uniqueTokenProviderService;

    @GetMapping("/getToken")
    public String getToken() {
        return uniqueTokenProviderService.generateToken();
    }

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {

        String token =  request.getHeader("Authorization").substring(7);

        model.addAttribute("username", jwtUtils.getUsernameFromJwt(token));

        return "home";
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request) {

        String token = jwtUtils.getJWTFromRequest(request);

        if (token == null) {
            System.out.println("non authenticated User viewed Login");
            return "login";
        }

        try {

            String username = jwtUtils.getUsernameFromJwt(token);

            System.out.println("user '" + username + "' viewed the Login Page, redirecting to logout.");

            return "redirect:/api/auth/logout";

        } catch (Exception e) {

            System.out.println("non authenticated User with Bearer token viewed Login");

            return "login";


        }

    }

}
