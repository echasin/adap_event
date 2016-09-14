package com.innvo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.innvo.domain.Alert;
import com.innvo.domain.Event;
import com.innvo.repository.AlertRepository;
import com.innvo.repository.search.AlertSearchRepository;
import com.innvo.web.rest.util.HeaderUtil;
import com.innvo.web.rest.util.PaginationUtil;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Topic;
import javax.naming.*;
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
 * REST controller for managing Alert.
 */
@RestController
@RequestMapping("/api")
public class AlertResource {
	
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	@Autowired
	private JmsMessagingTemplate jmsMessagingTemplate;

	@Autowired
	private Topic topic;

    private final Logger log = LoggerFactory.getLogger(AlertResource.class);
        
    @Inject
    private AlertRepository alertRepository;
    
    @Inject
    private AlertSearchRepository alertSearchRepository;
    
    /**
     * POST  /alerts : Create a new alert.
     *
     * @param alert the alert to create
     * @return the ResponseEntity with status 201 (Created) and with body the new alert, or with status 400 (Bad Request) if the alert has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/alerts",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Alert> createAlert(@Valid @RequestBody Alert alert) throws URISyntaxException {
        log.debug("REST request to save Alert : {}", alert);
        if (alert.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("alert", "idexists", "A new alert cannot already have an ID")).body(null);
        }
        Alert result = alertRepository.save(alert);
        alertSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/alerts/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("alert", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /alerts : Updates an existing alert.
     *
     * @param alert the alert to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated alert,
     * or with status 400 (Bad Request) if the alert is not valid,
     * or with status 500 (Internal Server Error) if the alert couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/alerts",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Alert> updateAlert(@Valid @RequestBody Alert alert) throws URISyntaxException {
        log.debug("REST request to update Alert : {}", alert);
        if (alert.getId() == null) {
            return createAlert(alert);
        }
        Alert result = alertRepository.save(alert);
        alertSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("alert", alert.getId().toString()))
            .body(result);
    }

    /**
     * GET  /alerts : get all the alerts.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of alerts in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/alerts",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Alert>> getAllAlerts(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Alerts");
        Page<Alert> page = alertRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/alerts");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /alerts/:id : get the "id" alert.
     *
     * @param id the id of the alert to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the alert, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/alerts/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Alert> getAlert(@PathVariable Long id) {
        log.debug("REST request to get Alert : {}", id);
        Alert alert = alertRepository.findOne(id);
        return Optional.ofNullable(alert)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /alerts/:id : delete the "id" alert.
     *
     * @param id the id of the alert to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/alerts/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        log.debug("REST request to delete Alert : {}", id);
        alertRepository.delete(id);
        alertSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("alert", id.toString())).build();
    }

    /**
     * SEARCH  /_search/alerts?query=:query : search for the alert corresponding
     * to the query.
     *
     * @param query the query of the alert search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/alerts",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Alert>> searchAlerts(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Alerts for query {}", query);
        Page<Alert> page = alertSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/alerts");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    
    /**
     * POST  /alerts : Create a new alert.
     *
     * @param alert the alert to create
     * @return the ResponseEntity with status 201 (Created) and with body the new alert, or with status 400 (Bad Request) if the alert has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     * @throws JMSException 
     * @throws NamingException 
     */
    @RequestMapping(value = "/alerttojms",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    
	public ResponseEntity<Alert> alertJms(@Valid @RequestBody Alert alert)
			throws URISyntaxException, JMSException, NamingException {
		log.debug("REST request to alertJms Alert : {}", alert);
		String alertMessage = alert.toString();
		String modifiedMsg = alertMessage.replace("Alert", "");
		this.jmsMessagingTemplate.convertAndSend(this.topic, modifiedMsg);
		return null;
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
		addMinutes.add(Calendar.MINUTE, 10);
		String startTime = sdFormat2.format(addMinutes.getTime());
		
		Calendar subMinutes = Calendar.getInstance();
		subMinutes.setTime(date);
		subMinutes.add(Calendar.MINUTE, -10);
		String endTime = sdFormat2.format(subMinutes.getTime());
		
		Calendar calendar = Calendar.getInstance(Locale.getDefault());
		
		LocalDateTime localStartTime = LocalDateTime.parse(startTime);
		LocalDateTime localEndTime = LocalDateTime.parse(endTime);
		
		ZoneId zoneId = ZoneId.of(calendar.getTimeZone().getID());
		
		ZonedDateTime startdateTime = ZonedDateTime.of(localStartTime, zoneId);
		ZonedDateTime enddateTime = ZonedDateTime.of(localEndTime, zoneId);
		
		log.debug("StartDateTime :" + startdateTime);
		log.debug("EndDateTime :" + enddateTime);
		List<Event> events = alertRepository.findEventDates(enddateTime, startdateTime);
		log.debug("Result :" + events);
		return events;

	}

}
