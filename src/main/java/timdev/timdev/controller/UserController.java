package timdev.timdev.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.entity.User;
import timdev.timdev.enums.RoleType;
import timdev.timdev.service.UserService;

@Controller
@RequestMapping("/admin/users")
// @PreAuthorize("hasRole('ADMIN')")
public class UserController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.getUsersNotInRoles(List.of(RoleType.USER));
        model.addAttribute("users", users);
        model.addAttribute("roleTypes", RoleType.values());
        return "admin/users/list";
    }
    
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roleTypes", RoleType.values());
        return "admin/users/form";
    }
    
    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute("user") User user, 
                           BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roleTypes", RoleType.values());
            return "admin/users/form";
        }
    
        if (userService.existsByUsername(user.getUsername())) {
            model.addAttribute("usernameError", "Username already exists");
            model.addAttribute("roleTypes", RoleType.values());
            return "admin/users/form";
        }
        
        if (userService.existsByEmail(user.getEmail())) {
            model.addAttribute("emailError", "Email already exists");
            model.addAttribute("roleTypes", RoleType.values());
            return "admin/users/form";
        }
        
        userService.saveUser(user);
        return "redirect:/admin/users?success=created";
    }

    @GetMapping("/profile")
    public String showProfileForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        
        model.addAttribute("user", user);
        return "admin/users/profile";
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));
        
        model.addAttribute("user", user);
        model.addAttribute("roleTypes", RoleType.values());
        return "admin/users/form";
    }

    @PostMapping("/profile/{id}")
    public String updateUserProfile(@PathVariable Long id, 
                           @Valid @ModelAttribute("user") User userDetails,
                           BindingResult result, @AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        if (result.hasErrors()) {
            return "admin/users/profile";
        }
        
        User existingUser = currentUser.getUser();
        
        // Check if username is changed and already exists
        if (!existingUser.getUsername().equals(userDetails.getUsername()) && 
            userService.existsByUsername(userDetails.getUsername())) {
            model.addAttribute("usernameError", "Username already exists");
            return "admin/users/profile";
        }
        
        // Check if email is changed and already exists
        if (!existingUser.getEmail().equals(userDetails.getEmail()) && 
            userService.existsByEmail(userDetails.getEmail())) {
            model.addAttribute("emailError", "Email already exists");
            return "admin/users/profile";
        }
        
        // Update user details
        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setUsername(userDetails.getUsername());
        existingUser.setPhoneNumber(userDetails.getPhoneNumber());
        existingUser.setActive(userDetails.isActive());
        
        // Only update password if it's not empty
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        
        userService.updateUser(existingUser);
        return "redirect:/dashboard";
    }
    
    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Long id, 
                           @Valid @ModelAttribute("user") User userDetails,
                           BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roleTypes", RoleType.values());
            return "admin/users/form";
        }
        
        User existingUser = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));
        
        // Check if username is changed and already exists
        if (!existingUser.getUsername().equals(userDetails.getUsername()) && 
            userService.existsByUsername(userDetails.getUsername())) {
            model.addAttribute("usernameError", "Username already exists");
            model.addAttribute("roleTypes", RoleType.values());
            return "admin/users/form";
        }
        
        // Check if email is changed and already exists
        if (!existingUser.getEmail().equals(userDetails.getEmail()) && 
            userService.existsByEmail(userDetails.getEmail())) {
            model.addAttribute("emailError", "Email already exists");
            model.addAttribute("roleTypes", RoleType.values());
            return "admin/users/form";
        }
        
        // Update user details
        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setUsername(userDetails.getUsername());
        existingUser.setPhoneNumber(userDetails.getPhoneNumber());
        existingUser.setActive(userDetails.isActive());
        
        // Update role based on user type
        existingUser.setRole(userDetails.getRole());
        
        // Only update password if it's not empty
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        
        userService.updateUser(existingUser);
        return "redirect:/admin/users?success=updated";
    }
    
    @PostMapping("/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));
        
        user.setActive(!user.isActive());
        userService.updateUser(user);
        
        return "redirect:/admin/users?success=status_updated";
    }
    
    
}