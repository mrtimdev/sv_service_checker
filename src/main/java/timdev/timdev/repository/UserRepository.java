package timdev.timdev.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import timdev.timdev.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    @Override
    Optional<User> findById(Long id);
}