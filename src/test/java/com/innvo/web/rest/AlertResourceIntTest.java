package com.innvo.web.rest;

import com.innvo.AdapEventApp;
import com.innvo.domain.Alert;
import com.innvo.repository.AlertRepository;
import com.innvo.repository.search.AlertSearchRepository;

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
 * Test class for the AlertResource REST controller.
 *
 * @see AlertResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AdapEventApp.class)
@WebAppConfiguration
@IntegrationTest
public class AlertResourceIntTest {

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
    private AlertRepository alertRepository;

    @Inject
    private AlertSearchRepository alertSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restAlertMockMvc;

    private Alert alert;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        AlertResource alertResource = new AlertResource();
        ReflectionTestUtils.setField(alertResource, "alertSearchRepository", alertSearchRepository);
        ReflectionTestUtils.setField(alertResource, "alertRepository", alertRepository);
        this.restAlertMockMvc = MockMvcBuilders.standaloneSetup(alertResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        alertSearchRepository.deleteAll();
        alert = new Alert();
        alert.setName(DEFAULT_NAME);
        alert.setDescription(DEFAULT_DESCRIPTION);
        alert.setCategory(DEFAULT_CATEGORY);
        alert.setSubcategory(DEFAULT_SUBCATEGORY);
        alert.setSubtype(DEFAULT_SUBTYPE);
        alert.setType(DEFAULT_TYPE);
        alert.setStartdatetime(DEFAULT_STARTDATETIME);
        alert.setEnddatetime(DEFAULT_ENDDATETIME);
        alert.setStatus(DEFAULT_STATUS);
        alert.setLastmodifiedby(DEFAULT_LASTMODIFIEDBY);
        alert.setLastmodifieddatetime(DEFAULT_LASTMODIFIEDDATETIME);
        alert.setDomain(DEFAULT_DOMAIN);
    }

    @Test
    @Transactional
    public void createAlert() throws Exception {
        int databaseSizeBeforeCreate = alertRepository.findAll().size();

        // Create the Alert

        restAlertMockMvc.perform(post("/api/alerts")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(alert)))
                .andExpect(status().isCreated());

        // Validate the Alert in the database
        List<Alert> alerts = alertRepository.findAll();
        assertThat(alerts).hasSize(databaseSizeBeforeCreate + 1);
        Alert testAlert = alerts.get(alerts.size() - 1);
        assertThat(testAlert.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAlert.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testAlert.getCategory()).isEqualTo(DEFAULT_CATEGORY);
        assertThat(testAlert.getSubcategory()).isEqualTo(DEFAULT_SUBCATEGORY);
        assertThat(testAlert.getSubtype()).isEqualTo(DEFAULT_SUBTYPE);
        assertThat(testAlert.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testAlert.getStartdatetime()).isEqualTo(DEFAULT_STARTDATETIME);
        assertThat(testAlert.getEnddatetime()).isEqualTo(DEFAULT_ENDDATETIME);
        assertThat(testAlert.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testAlert.getLastmodifiedby()).isEqualTo(DEFAULT_LASTMODIFIEDBY);
        assertThat(testAlert.getLastmodifieddatetime()).isEqualTo(DEFAULT_LASTMODIFIEDDATETIME);
        assertThat(testAlert.getDomain()).isEqualTo(DEFAULT_DOMAIN);

        // Validate the Alert in ElasticSearch
        Alert alertEs = alertSearchRepository.findOne(testAlert.getId());
        assertThat(alertEs).isEqualToComparingFieldByField(testAlert);
    }

    @Test
    @Transactional
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = alertRepository.findAll().size();
        // set the field null
        alert.setStatus(null);

        // Create the Alert, which fails.

        restAlertMockMvc.perform(post("/api/alerts")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(alert)))
                .andExpect(status().isBadRequest());

        List<Alert> alerts = alertRepository.findAll();
        assertThat(alerts).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLastmodifiedbyIsRequired() throws Exception {
        int databaseSizeBeforeTest = alertRepository.findAll().size();
        // set the field null
        alert.setLastmodifiedby(null);

        // Create the Alert, which fails.

        restAlertMockMvc.perform(post("/api/alerts")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(alert)))
                .andExpect(status().isBadRequest());

        List<Alert> alerts = alertRepository.findAll();
        assertThat(alerts).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLastmodifieddatetimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = alertRepository.findAll().size();
        // set the field null
        alert.setLastmodifieddatetime(null);

        // Create the Alert, which fails.

        restAlertMockMvc.perform(post("/api/alerts")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(alert)))
                .andExpect(status().isBadRequest());

        List<Alert> alerts = alertRepository.findAll();
        assertThat(alerts).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDomainIsRequired() throws Exception {
        int databaseSizeBeforeTest = alertRepository.findAll().size();
        // set the field null
        alert.setDomain(null);

        // Create the Alert, which fails.

        restAlertMockMvc.perform(post("/api/alerts")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(alert)))
                .andExpect(status().isBadRequest());

        List<Alert> alerts = alertRepository.findAll();
        assertThat(alerts).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllAlerts() throws Exception {
        // Initialize the database
        alertRepository.saveAndFlush(alert);

        // Get all the alerts
        restAlertMockMvc.perform(get("/api/alerts?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(alert.getId().intValue())))
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
    public void getAlert() throws Exception {
        // Initialize the database
        alertRepository.saveAndFlush(alert);

        // Get the alert
        restAlertMockMvc.perform(get("/api/alerts/{id}", alert.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(alert.getId().intValue()))
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
    public void getNonExistingAlert() throws Exception {
        // Get the alert
        restAlertMockMvc.perform(get("/api/alerts/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAlert() throws Exception {
        // Initialize the database
        alertRepository.saveAndFlush(alert);
        alertSearchRepository.save(alert);
        int databaseSizeBeforeUpdate = alertRepository.findAll().size();

        // Update the alert
        Alert updatedAlert = new Alert();
        updatedAlert.setId(alert.getId());
        updatedAlert.setName(UPDATED_NAME);
        updatedAlert.setDescription(UPDATED_DESCRIPTION);
        updatedAlert.setCategory(UPDATED_CATEGORY);
        updatedAlert.setSubcategory(UPDATED_SUBCATEGORY);
        updatedAlert.setSubtype(UPDATED_SUBTYPE);
        updatedAlert.setType(UPDATED_TYPE);
        updatedAlert.setStartdatetime(UPDATED_STARTDATETIME);
        updatedAlert.setEnddatetime(UPDATED_ENDDATETIME);
        updatedAlert.setStatus(UPDATED_STATUS);
        updatedAlert.setLastmodifiedby(UPDATED_LASTMODIFIEDBY);
        updatedAlert.setLastmodifieddatetime(UPDATED_LASTMODIFIEDDATETIME);
        updatedAlert.setDomain(UPDATED_DOMAIN);

        restAlertMockMvc.perform(put("/api/alerts")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedAlert)))
                .andExpect(status().isOk());

        // Validate the Alert in the database
        List<Alert> alerts = alertRepository.findAll();
        assertThat(alerts).hasSize(databaseSizeBeforeUpdate);
        Alert testAlert = alerts.get(alerts.size() - 1);
        assertThat(testAlert.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAlert.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testAlert.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testAlert.getSubcategory()).isEqualTo(UPDATED_SUBCATEGORY);
        assertThat(testAlert.getSubtype()).isEqualTo(UPDATED_SUBTYPE);
        assertThat(testAlert.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testAlert.getStartdatetime()).isEqualTo(UPDATED_STARTDATETIME);
        assertThat(testAlert.getEnddatetime()).isEqualTo(UPDATED_ENDDATETIME);
        assertThat(testAlert.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testAlert.getLastmodifiedby()).isEqualTo(UPDATED_LASTMODIFIEDBY);
        assertThat(testAlert.getLastmodifieddatetime()).isEqualTo(UPDATED_LASTMODIFIEDDATETIME);
        assertThat(testAlert.getDomain()).isEqualTo(UPDATED_DOMAIN);

        // Validate the Alert in ElasticSearch
        Alert alertEs = alertSearchRepository.findOne(testAlert.getId());
        assertThat(alertEs).isEqualToComparingFieldByField(testAlert);
    }

    @Test
    @Transactional
    public void deleteAlert() throws Exception {
        // Initialize the database
        alertRepository.saveAndFlush(alert);
        alertSearchRepository.save(alert);
        int databaseSizeBeforeDelete = alertRepository.findAll().size();

        // Get the alert
        restAlertMockMvc.perform(delete("/api/alerts/{id}", alert.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean alertExistsInEs = alertSearchRepository.exists(alert.getId());
        assertThat(alertExistsInEs).isFalse();

        // Validate the database is empty
        List<Alert> alerts = alertRepository.findAll();
        assertThat(alerts).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchAlert() throws Exception {
        // Initialize the database
        alertRepository.saveAndFlush(alert);
        alertSearchRepository.save(alert);

        // Search the alert
        restAlertMockMvc.perform(get("/api/_search/alerts?query=id:" + alert.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(alert.getId().intValue())))
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
