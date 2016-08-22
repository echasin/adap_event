package com.innvo.repository;

import com.innvo.domain.Alert;
import com.innvo.domain.Event;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for the Alert entity.
 */
public interface AlertRepository extends JpaRepository<Alert,Long> {

	@Query("SELECT e FROM Event e WHERE e.startdatetime BETWEEN :startdatetime AND :startdatetime1")
    public List<Event> findEventDates(@Param("startdatetime") ZonedDateTime startdatetime, @Param("startdatetime1") ZonedDateTime startdatetime1);

}
