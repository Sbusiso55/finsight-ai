package com.finsight.repository;

import com.finsight.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Database access for users (login, signup checks). */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
