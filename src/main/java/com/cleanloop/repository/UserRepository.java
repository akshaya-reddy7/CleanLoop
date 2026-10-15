package com.cleanloop.repository;

import com.cleanloop.entity.Role;
import com.cleanloop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByRole(Role role, Pageable pageable);

    List<User> findByRoleAndIsActiveTrue(Role role);

    List<User> findByRoleAndServiceAreaAndIsActiveTrue(Role role, String serviceArea);

    long countByRole(Role role);

    long countByIsActive(boolean isActive);
}
