package com.nearPg.Authentication_NearPg.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nearPg.Authentication_NearPg.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByRoleName(String string);

}
