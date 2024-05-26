package com.aweni.aweniBack;

import com.aweni.aweniBack.model.Role;
import com.aweni.aweniBack.repository.IRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Optional;
@RequiredArgsConstructor
@Slf4j

@SpringBootApplication
public class AweniBackApplication {
	private final IRoleRepository roleRepository;

	public static void main(String[] args) {
		SpringApplication.run(AweniBackApplication.class, args);
	}

	@Bean
	CommandLineRunner start() {
		return args -> {
			Role association = new Role();
			association.setName("Association");
			association.setDescription("Association");
			saveRole(association);
			Role user = new Role();
			user.setName("Utilisateur");
			user.setDescription("Utilisateur");
			saveRole(user);
		};};
			private void saveRole(Role role) {
				Optional<Role> roleSearched = roleRepository.findByName(role.getName());
				if (roleSearched.isEmpty()) {
					role = roleRepository.save(role);
					log.info("The role with name '{}' SAVED.", role.getName());
				} else {
					log.info("The role with name '{}' FOUND.", role.getName());
				}
			}
}
