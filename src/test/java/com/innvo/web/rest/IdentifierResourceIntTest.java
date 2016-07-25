package com.innvo.web.rest;

import com.innvo.AdapEventApp;
import com.innvo.domain.Identifier;
import com.innvo.repository.IdentifierRepository;
import com.innvo.repository.search.IdentifierSearchRepository;

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
 * Test class for the IdentifierResource REST controller.
 *
 * @see IdentifierResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AdapEventApp.class)
@WebAppConfiguration
@IntegrationTest
public class IdentifierResourceIntTest {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("Z"));

    private static final String DEFAULT_TYPE = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_TYPE = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";
    private static final String DEFAULT_VALUE = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";
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
    private IdentifierRepository identifierRepository;

    @Inject
    private IdentifierSearchRepository identifierSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restIdentifierMockMvc;

    private Identifier identifier;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        IdentifierResource identifierResource = new IdentifierResource();
        ReflectionTestUtils.setField(identifierResource, "identifierSearchRepository", identifierSearchRepository);
        ReflectionTestUtils.setField(identifierResource, "identifierRepository", identifierRepository);
        this.restIdentifierMockMvc = MockMvcBuilders.standaloneSetup(identifierResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        identifierSearchRepository.deleteAll();
        identifier = new Identifier();
        identifier.setType(DEFAULT_TYPE);
        identifier.setValue(DEFAULT_VALUE);
        identifier.setStatus(DEFAULT_STATUS);
        identifier.setLastmodifiedby(DEFAULT_LASTMODIFIEDBY);
        identifier.setLastmodifieddatetime(DEFAULT_LASTMODIFIEDDATETIME);
        identifier.setDomain(DEFAULT_DOMAIN);
    }

    @Test
    @Transactional
    public void createIdentifier() throws Exception {
        int databaseSizeBeforeCreate = identifierRepository.findAll().size();

        // Create the Identifier

        restIdentifierMockMvc.perform(post("/api/identifiers")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(identifier)))
                .andExpect(status().isCreated());

        // Validate the Identifier in the database
        List<Identifier> identifiers = identifierRepository.findAll();
        assertThat(identifiers).hasSize(databaseSizeBeforeCreate + 1);
        Identifier testIdentifier = identifiers.get(identifiers.size() - 1);
        assertThat(testIdentifier.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testIdentifier.getValue()).isEqualTo(DEFAULT_VALUE);
        assertThat(testIdentifier.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testIdentifier.getLastmodifiedby()).isEqualTo(DEFAULT_LASTMODIFIEDBY);
        assertThat(testIdentifier.getLastmodifieddatetime()).isEqualTo(DEFAULT_LASTMODIFIEDDATETIME);
        assertThat(testIdentifier.getDomain()).isEqualTo(DEFAULT_DOMAIN);

        // Validate the Identifier in ElasticSearch
        Identifier identifierEs = identifierSearchRepository.findOne(testIdentifier.getId());
        assertThat(identifierEs).isEqualToComparingFieldByField(testIdentifier);
    }

    @Test
    @Transactional
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = identifierRepository.findAll().size();
        // set the field null
        identifier.setType(null);

        // Create the Identifier, which fails.

        restIdentifierMockMvc.perform(post("/api/identifiers")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(identifier)))
                .andExpect(status().isBadRequest());

        List<Identifier> identifiers = identifierRepository.findAll();
        assertThat(identifiers).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkValueIsRequired() throws Exception {
        int databaseSizeBeforeTest = identifierRepository.findAll().size();
        // set the field null
        identifier.setValue(null);

        // Create the Identifier, which fails.

        restIdentifierMockMvc.perform(post("/api/identifiers")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(identifier)))
                .andExpect(status().isBadRequest());

        List<Identifier> identifiers = identifierRepository.findAll();
        assertThat(identifiers).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = identifierRepository.findAll().size();
        // set the field null
        identifier.setStatus(null);

        // Create the Identifier, which fails.

        restIdentifierMockMvc.perform(post("/api/identifiers")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(identifier)))
                .andExpect(status().isBadRequest());

        List<Identifier> identifiers = identifierRepository.findAll();
        assertThat(identifiers).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLastmodifiedbyIsRequired() throws Exception {
        int databaseSizeBeforeTest = identifierRepository.findAll().size();
        // set the field null
        identifier.setLastmodifiedby(null);

        // Create the Identifier, which fails.

        restIdentifierMockMvc.perform(post("/api/identifiers")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(identifier)))
                .andExpect(status().isBadRequest());

        List<Identifier> identifiers = identifierRepository.findAll();
        assertThat(identifiers).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLastmodifieddatetimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = identifierRepository.findAll().size();
        // set the field null
        identifier.setLastmodifieddatetime(null);

        // Create the Identifier, which fails.

        restIdentifierMockMvc.perform(post("/api/identifiers")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(identifier)))
                .andExpect(status().isBadRequest());

        List<Identifier> identifiers = identifierRepository.findAll();
        assertThat(identifiers).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDomainIsRequired() throws Exception {
        int databaseSizeBeforeTest = identifierRepository.findAll().size();
        // set the field null
        identifier.setDomain(null);

        // Create the Identifier, which fails.

        restIdentifierMockMvc.perform(post("/api/identifiers")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(identifier)))
                .andExpect(status().isBadRequest());

        List<Identifier> identifiers = identifierRepository.findAll();
        assertThat(identifiers).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllIdentifiers() throws Exception {
        // Initialize the database
        identifierRepository.saveAndFlush(identifier);

        // Get all the identifiers
        restIdentifierMockMvc.perform(get("/api/identifiers?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(identifier.getId().intValue())))
                .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
                .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.toString())))
                .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
                .andExpect(jsonPath("$.[*].lastmodifiedby").value(hasItem(DEFAULT_LASTMODIFIEDBY.toString())))
                .andExpect(jsonPath("$.[*].lastmodifieddatetime").value(hasItem(DEFAULT_LASTMODIFIEDDATETIME_STR)))
                .andExpect(jsonPath("$.[*].domain").value(hasItem(DEFAULT_DOMAIN.toString())));
    }

    @Test
    @Transactional
    public void getIdentifier() throws Exception {
        // Initialize the database
        identifierRepository.saveAndFlush(identifier);

        // Get the identifier
        restIdentifierMockMvc.perform(get("/api/identifiers/{id}", identifier.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(identifier.getId().intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.lastmodifiedby").value(DEFAULT_LASTMODIFIEDBY.toString()))
            .andExpect(jsonPath("$.lastmodifieddatetime").value(DEFAULT_LASTMODIFIEDDATETIME_STR))
            .andExpect(jsonPath("$.domain").value(DEFAULT_DOMAIN.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingIdentifier() throws Exception {
        // Get the identifier
        restIdentifierMockMvc.perform(get("/api/identifiers/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateIdentifier() throws Exception {
        // Initialize the database
        identifierRepository.saveAndFlush(identifier);
        identifierSearchRepository.save(identifier);
        int databaseSizeBeforeUpdate = identifierRepository.findAll().size();

        // Update the identifier
        Identifier updatedIdentifier = new Identifier();
        updatedIdentifier.setId(identifier.getId());
        updatedIdentifier.setType(UPDATED_TYPE);
        updatedIdentifier.setValue(UPDATED_VALUE);
        updatedIdentifier.setStatus(UPDATED_STATUS);
        updatedIdentifier.setLastmodifiedby(UPDATED_LASTMODIFIEDBY);
        updatedIdentifier.setLastmodifieddatetime(UPDATED_LASTMODIFIEDDATETIME);
        updatedIdentifier.setDomain(UPDATED_DOMAIN);

        restIdentifierMockMvc.perform(put("/api/identifiers")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedIdentifier)))
                .andExpect(status().isOk());

        // Validate the Identifier in the database
        List<Identifier> identifiers = identifierRepository.findAll();
        assertThat(identifiers).hasSize(databaseSizeBeforeUpdate);
        Identifier testIdentifier = identifiers.get(identifiers.size() - 1);
        assertThat(testIdentifier.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testIdentifier.getValue()).isEqualTo(UPDATED_VALUE);
        assertThat(testIdentifier.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testIdentifier.getLastmodifiedby()).isEqualTo(UPDATED_LASTMODIFIEDBY);
        assertThat(testIdentifier.getLastmodifieddatetime()).isEqualTo(UPDATED_LASTMODIFIEDDATETIME);
        assertThat(testIdentifier.getDomain()).isEqualTo(UPDATED_DOMAIN);

        // Validate the Identifier in ElasticSearch
        Identifier identifierEs = identifierSearchRepository.findOne(testIdentifier.getId());
        assertThat(identifierEs).isEqualToComparingFieldByField(testIdentifier);
    }

    @Test
    @Transactional
    public void deleteIdentifier() throws Exception {
        // Initialize the database
        identifierRepository.saveAndFlush(identifier);
        identifierSearchRepository.save(identifier);
        int databaseSizeBeforeDelete = identifierRepository.findAll().size();

        // Get the identifier
        restIdentifierMockMvc.perform(delete("/api/identifiers/{id}", identifier.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean identifierExistsInEs = identifierSearchRepository.exists(identifier.getId());
        assertThat(identifierExistsInEs).isFalse();

        // Validate the database is empty
        List<Identifier> identifiers = identifierRepository.findAll();
        assertThat(identifiers).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchIdentifier() throws Exception {
        // Initialize the database
        identifierRepository.saveAndFlush(identifier);
        identifierSearchRepository.save(identifier);

        // Search the identifier
        restIdentifierMockMvc.perform(get("/api/_search/identifiers?query=id:" + identifier.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(identifier.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].lastmodifiedby").value(hasItem(DEFAULT_LASTMODIFIEDBY.toString())))
            .andExpect(jsonPath("$.[*].lastmodifieddatetime").value(hasItem(DEFAULT_LASTMODIFIEDDATETIME_STR)))
            .andExpect(jsonPath("$.[*].domain").value(hasItem(DEFAULT_DOMAIN.toString())));
    }
}
