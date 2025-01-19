package com.nearPg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.nearPg.Authentication_NearPg.model.Role;
import com.nearPg.Authentication_NearPg.repository.RoleRepository;

@SpringBootApplication
public class NearPgApplication {

	private final RoleRepository roleRepository;

	@Autowired
	public NearPgApplication(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(NearPgApplication.class, args);
	}

	@Bean
	public CommandLineRunner run() {
		return args -> {
			// Check if the "USER" role already exists
			if (roleRepository.findByRoleName("USER") == null) {
				Role userRole = new Role("USER");
				roleRepository.save(userRole);
			}

			// Check if the "PG_OWNER" role already exists
			if (roleRepository.findByRoleName("PG_OWNER") == null) {
				Role pgOwnerRole = new Role("PG_OWNER");
				roleRepository.save(pgOwnerRole);
			}

			// Check if the "ADMIN" role already exists
			if (roleRepository.findByRoleName("ADMIN") == null) {
				Role adminRole = new Role("ADMIN");
				roleRepository.save(adminRole);
			}
		};
	}

}
