package com.innvo.repository;

import com.innvo.domain.Identifier;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Identifier entity.
 */
@SuppressWarnings("unused")
public interface IdentifierRepository extends JpaRepository<Identifier,Long> {

}
