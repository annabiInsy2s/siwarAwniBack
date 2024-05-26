package com.aweni.aweniBack.repository;

import com.aweni.aweniBack.model.Demande;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IDemandeRepository extends JpaRepository<Demande, Long > {
}
