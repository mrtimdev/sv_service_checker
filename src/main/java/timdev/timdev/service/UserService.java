package timdev.timdev.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import timdev.timdev.entity.User;
import timdev.timdev.enums.ApprovalLevel;
import timdev.timdev.enums.RoleType;
import timdev.timdev.enums.UserType;
import timdev.timdev.repository.UserRepository;

@Service
public class UserService {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public void assignApprovalLevel(User user, ApprovalLevel approvalLevel) {
        // This would typically update a field in the User entity
        // For demonstration, we'll assume there's an approvalLevel field
        user.setApprovalLevel(approvalLevel);
        userRepository.save(user);
    }


  
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }


    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

  
    public List<User> findByUserType(UserType userType) {
        return userRepository.findByUserType(userType);
    }

    public List<User> findByRole(RoleType role) {
        return userRepository.findByRole(role);
    }

    public List<User> getUsersNotInRoles(List<RoleType> excludedRoles) {
        return userRepository.findByRoleNotIn(excludedRoles);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }


    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }


    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }


    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }


    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}
