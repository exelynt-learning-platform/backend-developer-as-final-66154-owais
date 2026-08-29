package com.example.bookingsystem.repository;

import com.example.bookingsystem.entity.Role;
import com.example.bookingsystem.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
