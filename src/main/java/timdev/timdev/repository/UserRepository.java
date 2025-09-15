package timdev.timdev.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import timdev.timdev.entity.User;
import timdev.timdev.enums.RoleType;
import timdev.timdev.enums.UserType;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    @SuppressWarnings("null")
    @Override
    Optional<User> findById(Long id);

    Optional<User> findByUsernameOrEmail(String username, String email);

    List<User> findByRole(RoleType role);
    List<User> findByUserType(UserType userType);

    List<User> findByRoleNotIn(List<RoleType> roles);


    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

}