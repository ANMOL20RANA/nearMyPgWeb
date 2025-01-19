package com.nearPg.Authentication_NearPg.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nearPg.Authentication_NearPg.model.Users;

public interface UsersRepository extends JpaRepository<Users, Long> {

    // Users findByEmail(String email);

    Optional<Users> findByEmail(String email);

    Optional<Users> findByUsername(String username);

}