package com.innvo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.innvo.domain.Event;
import com.innvo.repository.EventRepository;
import com.innvo.repository.search.EventSearchRepository;
import com.innvo.web.rest.util.HeaderUtil;
import com.innvo.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Event.
 */
@RestController
@RequestMapping("/api")
public class EventResource {

    private final Logger log = LoggerFactory.getLogger(EventResource.class);
    
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        
    @Inject
    private EventRepository eventRepository;
        
    @Inject
    private EventSearchRepository eventSearchRepository;
    
    /**
     * POST  /events : Create a new event.
     *
     * @param event the event to create
     * @return the ResponseEntity with status 201 (Created) and with body the new event, or with status 400 (Bad Request) if the event has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/events",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Event> createEvent(@Valid @RequestBody Event event) throws URISyntaxException {
        log.debug("REST request to save Event : {}", event);
        if (event.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("event", "idexists", "A new event cannot already have an ID")).body(null);
        }
        Event result = eventRepository.save(event);
        eventSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/events/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("event", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /events : Updates an existing event.
     *
     * @param event the event to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated event,
     * or with status 400 (Bad Request) if the event is not valid,
     * or with status 500 (Internal Server Error) if the event couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/events",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Event> updateEvent(@Valid @RequestBody Event event) throws URISyntaxException {
        log.debug("REST request to update Event : {}", event);
        if (event.getId() == null) {
            return createEvent(event);
        }
        Event result = eventRepository.save(event);
        eventSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("event", event.getId().toString()))
            .body(result);
    }

    /**
     * GET  /events : get all the events.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of events in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/events",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Event>> getAllEvents(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Events");
        Page<Event> page = eventRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/events");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /events/:id : get the "id" event.
     *
     * @param id the id of the event to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the event, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/events/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Event> getEvent(@PathVariable Long id) {
        log.debug("REST request to get Event : {}", id);
        Event event = eventRepository.findOne(id);
        return Optional.ofNullable(event)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /events/:id : delete the "id" event.
     *
     * @param id the id of the event to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/events/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        log.debug("REST request to delete Event : {}", id);
        eventRepository.delete(id);
        eventSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("event", id.toString())).build();
    }

    /**
     * SEARCH  /_search/events?query=:query : search for the event corresponding
     * to the query.
     *
     * @param query the query of the event search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/events",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Event>> searchEvents(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Events for query {}", query);
        Page<Event> page = eventSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/events");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /alerts/:id : get the "id" alert.
     *
     * @param id the id of the alert to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the alert, or with status 404 (Not Found)
     * @throws ParseException 
     */
    @RequestMapping(value = "/eventobject/{startDateTime}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
	public List<Event> getEvents(@PathVariable String startDateTime) throws ParseException {

		ZonedDateTime stringDate = ZonedDateTime.parse(startDateTime);
		Date stringToDate = Date.from(stringDate.toInstant());
		SimpleDateFormat sdFormat1 = new SimpleDateFormat(DATE_FORMAT);
		String myTime = sdFormat1.format(stringToDate);
		String replaceZone = myTime.replace("Z", "");
		SimpleDateFormat sdFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
		Date date = sdFormat2.parse(replaceZone);
		
		Calendar addMinutes = Calendar.getInstance();
		addMinutes.setTime(date);
		addMinutes.add(Calendar.MINUTE, 3);
		String startTime = sdFormat2.format(addMinutes.getTime());
		
		Calendar subMinutes = Calendar.getInstance();
		subMinutes.setTime(date);
		subMinutes.add(Calendar.MINUTE, -3);
		String endTime = sdFormat2.format(subMinutes.getTime());
		
		Calendar calendar = Calendar.getInstance(Locale.getDefault());
		
		LocalDateTime localStartTime = LocalDateTime.parse(startTime);
		LocalDateTime localEndTime = LocalDateTime.parse(endTime);
		
		ZoneId zoneId = ZoneId.of(calendar.getTimeZone().getID());
		
		ZonedDateTime startdateTime = ZonedDateTime.of(localStartTime, zoneId);
		ZonedDateTime enddateTime = ZonedDateTime.of(localEndTime, zoneId);
		
		log.debug("StartDateTime :" + startdateTime);
		log.debug("EndDateTime :" + enddateTime);
		List<Event> events = eventRepository.findEventDates(enddateTime, startdateTime);
		log.debug("Result :" + events);
		return events;

	}


}
