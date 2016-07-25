package com.innvo.web.rest;

import com.innvo.AdapEventApp;
import com.innvo.domain.Event;
import com.innvo.repository.EventRepository;
import com.innvo.repository.search.EventSearchRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the EventResource REST controller.
 *
 * @see EventResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AdapEventApp.class)
@WebAppConfiguration
@IntegrationTest
public class EventResourceIntTest {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("Z"));

    private static final String DEFAULT_NAME = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";
    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";
    private static final String DEFAULT_CATEGORY = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_CATEGORY = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";
    private static final String DEFAULT_SUBCATEGORY = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_SUBCATEGORY = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";
    private static final String DEFAULT_SUBTYPE = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_SUBTYPE = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";
    private static final String DEFAULT_TYPE = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_TYPE = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_STARTDATETIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
    private static final ZonedDateTime UPDATED_STARTDATETIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final String DEFAULT_STARTDATETIME_STR = dateTimeFormatter.format(DEFAULT_STARTDATETIME);

    private static final ZonedDateTime DEFAULT_ENDDATETIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
    private static final ZonedDateTime UPDATED_ENDDATETIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final String DEFAULT_ENDDATETIME_STR = dateTimeFormatter.format(DEFAULT_ENDDATETIME);
    private static final String DEFAULT_STATUS = "AAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBBBBBBBBBBBBBBBBB";
    private static final String DEFAULT_LASTMODIFIEDBY = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_LASTMODIFIEDBY = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_LASTMODIFIEDDATETIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
    private static final ZonedDateTime UPDATED_LASTMODIFIEDDATETIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final String DEFAULT_LASTMODIFIEDDATETIME_STR = dateTimeFormatter.format(DEFAULT_LASTMODIFIEDDATETIME);
    private static final String DEFAULT_DOMAIN = "AAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_DOMAIN = "BBBBBBBBBBBBBBBBBBBBBBBBB";

    @Inject
    private EventRepository eventRepository;

    @Inject
    private EventSearchRepository eventSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restEventMockMvc;

    private Event event;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        EventResource eventResource = new EventResource();
        ReflectionTestUtils.setField(eventResource, "eventSearchRepository", eventSearchRepository);
        ReflectionTestUtils.setField(eventResource, "eventRepository", eventRepository);
        this.restEventMockMvc = MockMvcBuilders.standaloneSetup(eventResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        eventSearchRepository.deleteAll();
        event = new Event();
        event.setName(DEFAULT_NAME);
        event.setDescription(DEFAULT_DESCRIPTION);
        event.setCategory(DEFAULT_CATEGORY);
        event.setSubcategory(DEFAULT_SUBCATEGORY);
        event.setSubtype(DEFAULT_SUBTYPE);
        event.setType(DEFAULT_TYPE);
        event.setStartdatetime(DEFAULT_STARTDATETIME);
        event.setEnddatetime(DEFAULT_ENDDATETIME);
        event.setStatus(DEFAULT_STATUS);
        event.setLastmodifiedby(DEFAULT_LASTMODIFIEDBY);
        event.setLastmodifieddatetime(DEFAULT_LASTMODIFIEDDATETIME);
        event.setDomain(DEFAULT_DOMAIN);
    }

    @Test
    @Transactional
    public void createEvent() throws Exception {
        int databaseSizeBeforeCreate = eventRepository.findAll().size();

        // Create the Event

        restEventMockMvc.perform(post("/api/events")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(event)))
                .andExpect(status().isCreated());

        // Validate the Event in the database
        List<Event> events = eventRepository.findAll();
        assertThat(events).hasSize(databaseSizeBeforeCreate + 1);
        Event testEvent = events.get(events.size() - 1);
        assertThat(testEvent.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testEvent.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testEvent.getCategory()).isEqualTo(DEFAULT_CATEGORY);
        assertThat(testEvent.getSubcategory()).isEqualTo(DEFAULT_SUBCATEGORY);
        assertThat(testEvent.getSubtype()).isEqualTo(DEFAULT_SUBTYPE);
        assertThat(testEvent.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testEvent.getStartdatetime()).isEqualTo(DEFAULT_STARTDATETIME);
        assertThat(testEvent.getEnddatetime()).isEqualTo(DEFAULT_ENDDATETIME);
        assertThat(testEvent.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testEvent.getLastmodifiedby()).isEqualTo(DEFAULT_LASTMODIFIEDBY);
        assertThat(testEvent.getLastmodifieddatetime()).isEqualTo(DEFAULT_LASTMODIFIEDDATETIME);
        assertThat(testEvent.getDomain()).isEqualTo(DEFAULT_DOMAIN);

        // Validate the Event in ElasticSearch
        Event eventEs = eventSearchRepository.findOne(testEvent.getId());
        assertThat(eventEs).isEqualToComparingFieldByField(testEvent);
    }

    @Test
    @Transactional
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = eventRepository.findAll().size();
        // set the field null
        event.setStatus(null);

        // Create the Event, which fails.

        restEventMockMvc.perform(post("/api/events")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(event)))
                .andExpect(status().isBadRequest());

        List<Event> events = eventRepository.findAll();
        assertThat(events).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLastmodifiedbyIsRequired() throws Exception {
        int databaseSizeBeforeTest = eventRepository.findAll().size();
        // set the field null
        event.setLastmodifiedby(null);

        // Create the Event, which fails.

        restEventMockMvc.perform(post("/api/events")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(event)))
                .andExpect(status().isBadRequest());

        List<Event> events = eventRepository.findAll();
        assertThat(events).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLastmodifieddatetimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = eventRepository.findAll().size();
        // set the field null
        event.setLastmodifieddatetime(null);

        // Create the Event, which fails.

        restEventMockMvc.perform(post("/api/events")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(event)))
                .andExpect(status().isBadRequest());

        List<Event> events = eventRepository.findAll();
        assertThat(events).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDomainIsRequired() throws Exception {
        int databaseSizeBeforeTest = eventRepository.findAll().size();
        // set the field null
        event.setDomain(null);

        // Create the Event, which fails.

        restEventMockMvc.perform(post("/api/events")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(event)))
                .andExpect(status().isBadRequest());

        List<Event> events = eventRepository.findAll();
        assertThat(events).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllEvents() throws Exception {
        // Initialize the database
        eventRepository.saveAndFlush(event);

        // Get all the events
        restEventMockMvc.perform(get("/api/events?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(event.getId().intValue())))
                .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
                .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
                .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY.toString())))
                .andExpect(jsonPath("$.[*].subcategory").value(hasItem(DEFAULT_SUBCATEGORY.toString())))
                .andExpect(jsonPath("$.[*].subtype").value(hasItem(DEFAULT_SUBTYPE.toString())))
                .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
                .andExpect(jsonPath("$.[*].startdatetime").value(hasItem(DEFAULT_STARTDATETIME_STR)))
                .andExpect(jsonPath("$.[*].enddatetime").value(hasItem(DEFAULT_ENDDATETIME_STR)))
                .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
                .andExpect(jsonPath("$.[*].lastmodifiedby").value(hasItem(DEFAULT_LASTMODIFIEDBY.toString())))
                .andExpect(jsonPath("$.[*].lastmodifieddatetime").value(hasItem(DEFAULT_LASTMODIFIEDDATETIME_STR)))
                .andExpect(jsonPath("$.[*].domain").value(hasItem(DEFAULT_DOMAIN.toString())));
    }

    @Test
    @Transactional
    public void getEvent() throws Exception {
        // Initialize the database
        eventRepository.saveAndFlush(event);

        // Get the event
        restEventMockMvc.perform(get("/api/events/{id}", event.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(event.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY.toString()))
            .andExpect(jsonPath("$.subcategory").value(DEFAULT_SUBCATEGORY.toString()))
            .andExpect(jsonPath("$.subtype").value(DEFAULT_SUBTYPE.toString()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.startdatetime").value(DEFAULT_STARTDATETIME_STR))
            .andExpect(jsonPath("$.enddatetime").value(DEFAULT_ENDDATETIME_STR))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.lastmodifiedby").value(DEFAULT_LASTMODIFIEDBY.toString()))
            .andExpect(jsonPath("$.lastmodifieddatetime").value(DEFAULT_LASTMODIFIEDDATETIME_STR))
            .andExpect(jsonPath("$.domain").value(DEFAULT_DOMAIN.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingEvent() throws Exception {
        // Get the event
        restEventMockMvc.perform(get("/api/events/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateEvent() throws Exception {
        // Initialize the database
        eventRepository.saveAndFlush(event);
        eventSearchRepository.save(event);
        int databaseSizeBeforeUpdate = eventRepository.findAll().size();

        // Update the event
        Event updatedEvent = new Event();
        updatedEvent.setId(event.getId());
        updatedEvent.setName(UPDATED_NAME);
        updatedEvent.setDescription(UPDATED_DESCRIPTION);
        updatedEvent.setCategory(UPDATED_CATEGORY);
        updatedEvent.setSubcategory(UPDATED_SUBCATEGORY);
        updatedEvent.setSubtype(UPDATED_SUBTYPE);
        updatedEvent.setType(UPDATED_TYPE);
        updatedEvent.setStartdatetime(UPDATED_STARTDATETIME);
        updatedEvent.setEnddatetime(UPDATED_ENDDATETIME);
        updatedEvent.setStatus(UPDATED_STATUS);
        updatedEvent.setLastmodifiedby(UPDATED_LASTMODIFIEDBY);
        updatedEvent.setLastmodifieddatetime(UPDATED_LASTMODIFIEDDATETIME);
        updatedEvent.setDomain(UPDATED_DOMAIN);

        restEventMockMvc.perform(put("/api/events")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedEvent)))
                .andExpect(status().isOk());

        // Validate the Event in the database
        List<Event> events = eventRepository.findAll();
        assertThat(events).hasSize(databaseSizeBeforeUpdate);
        Event testEvent = events.get(events.size() - 1);
        assertThat(testEvent.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testEvent.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testEvent.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testEvent.getSubcategory()).isEqualTo(UPDATED_SUBCATEGORY);
        assertThat(testEvent.getSubtype()).isEqualTo(UPDATED_SUBTYPE);
        assertThat(testEvent.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testEvent.getStartdatetime()).isEqualTo(UPDATED_STARTDATETIME);
        assertThat(testEvent.getEnddatetime()).isEqualTo(UPDATED_ENDDATETIME);
        assertThat(testEvent.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testEvent.getLastmodifiedby()).isEqualTo(UPDATED_LASTMODIFIEDBY);
        assertThat(testEvent.getLastmodifieddatetime()).isEqualTo(UPDATED_LASTMODIFIEDDATETIME);
        assertThat(testEvent.getDomain()).isEqualTo(UPDATED_DOMAIN);

        // Validate the Event in ElasticSearch
        Event eventEs = eventSearchRepository.findOne(testEvent.getId());
        assertThat(eventEs).isEqualToComparingFieldByField(testEvent);
    }

    @Test
    @Transactional
    public void deleteEvent() throws Exception {
        // Initialize the database
        eventRepository.saveAndFlush(event);
        eventSearchRepository.save(event);
        int databaseSizeBeforeDelete = eventRepository.findAll().size();

        // Get the event
        restEventMockMvc.perform(delete("/api/events/{id}", event.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean eventExistsInEs = eventSearchRepository.exists(event.getId());
        assertThat(eventExistsInEs).isFalse();

        // Validate the database is empty
        List<Event> events = eventRepository.findAll();
        assertThat(events).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchEvent() throws Exception {
        // Initialize the database
        eventRepository.saveAndFlush(event);
        eventSearchRepository.save(event);

        // Search the event
        restEventMockMvc.perform(get("/api/_search/events?query=id:" + event.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(event.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY.toString())))
            .andExpect(jsonPath("$.[*].subcategory").value(hasItem(DEFAULT_SUBCATEGORY.toString())))
            .andExpect(jsonPath("$.[*].subtype").value(hasItem(DEFAULT_SUBTYPE.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].startdatetime").value(hasItem(DEFAULT_STARTDATETIME_STR)))
            .andExpect(jsonPath("$.[*].enddatetime").value(hasItem(DEFAULT_ENDDATETIME_STR)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].lastmodifiedby").value(hasItem(DEFAULT_LASTMODIFIEDBY.toString())))
            .andExpect(jsonPath("$.[*].lastmodifieddatetime").value(hasItem(DEFAULT_LASTMODIFIEDDATETIME_STR)))
            .andExpect(jsonPath("$.[*].domain").value(hasItem(DEFAULT_DOMAIN.toString())));
    }
}
