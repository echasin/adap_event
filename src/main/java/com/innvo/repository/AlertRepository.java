package com.innvo.repository;

import com.innvo.domain.Alert;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Alert entity.
 */
@SuppressWarnings("unused")
public interface AlertRepository extends JpaRepository<Alert,Long> {

}
