package com.vibesync.repository;

import com.vibesync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Used at login — find a user by their email address
    Optional<User> findByEmail(String email);

    // Used at registration — check if email is already taken
    boolean existsByEmail(String email);
}
