package com.aweni.aweniBack.repository;

import com.aweni.aweniBack.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Role} entity.
 */
@Repository
public interface IRoleRepository extends JpaRepository<Role, Long> {


    Optional<Role> findByName(String name);

}
