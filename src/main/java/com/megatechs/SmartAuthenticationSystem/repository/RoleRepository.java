package com.megatechs.SmartAuthenticationSystem.repository;

import com.megatechs.SmartAuthenticationSystem.models.AppUser;
import com.megatechs.SmartAuthenticationSystem.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<UserRole, Long> {
    Optional<UserRole> findByName(String name);
}
