package com.nearPg.Authentication_NearPg.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nearPg.Authentication_NearPg.model.TemraryOtp;

public interface TemprarayOtpRepository extends JpaRepository<TemraryOtp, Long> {

    Optional<TemraryOtp> findByEmail(String email);

}
