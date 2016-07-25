package com.innvo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.innvo.domain.Identifier;
import com.innvo.repository.IdentifierRepository;
import com.innvo.repository.search.IdentifierSearchRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Identifier.
 */
@RestController
@RequestMapping("/api")
public class IdentifierResource {

    private final Logger log = LoggerFactory.getLogger(IdentifierResource.class);
        
    @Inject
    private IdentifierRepository identifierRepository;
    
    @Inject
    private IdentifierSearchRepository identifierSearchRepository;
    
    /**
     * POST  /identifiers : Create a new identifier.
     *
     * @param identifier the identifier to create
     * @return the ResponseEntity with status 201 (Created) and with body the new identifier, or with status 400 (Bad Request) if the identifier has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/identifiers",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Identifier> createIdentifier(@Valid @RequestBody Identifier identifier) throws URISyntaxException {
        log.debug("REST request to save Identifier : {}", identifier);
        if (identifier.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("identifier", "idexists", "A new identifier cannot already have an ID")).body(null);
        }
        Identifier result = identifierRepository.save(identifier);
        identifierSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/identifiers/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("identifier", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /identifiers : Updates an existing identifier.
     *
     * @param identifier the identifier to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated identifier,
     * or with status 400 (Bad Request) if the identifier is not valid,
     * or with status 500 (Internal Server Error) if the identifier couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/identifiers",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Identifier> updateIdentifier(@Valid @RequestBody Identifier identifier) throws URISyntaxException {
        log.debug("REST request to update Identifier : {}", identifier);
        if (identifier.getId() == null) {
            return createIdentifier(identifier);
        }
        Identifier result = identifierRepository.save(identifier);
        identifierSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("identifier", identifier.getId().toString()))
            .body(result);
    }

    /**
     * GET  /identifiers : get all the identifiers.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of identifiers in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/identifiers",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Identifier>> getAllIdentifiers(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Identifiers");
        Page<Identifier> page = identifierRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/identifiers");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /identifiers/:id : get the "id" identifier.
     *
     * @param id the id of the identifier to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the identifier, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/identifiers/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Identifier> getIdentifier(@PathVariable Long id) {
        log.debug("REST request to get Identifier : {}", id);
        Identifier identifier = identifierRepository.findOne(id);
        return Optional.ofNullable(identifier)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /identifiers/:id : delete the "id" identifier.
     *
     * @param id the id of the identifier to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/identifiers/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteIdentifier(@PathVariable Long id) {
        log.debug("REST request to delete Identifier : {}", id);
        identifierRepository.delete(id);
        identifierSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("identifier", id.toString())).build();
    }

    /**
     * SEARCH  /_search/identifiers?query=:query : search for the identifier corresponding
     * to the query.
     *
     * @param query the query of the identifier search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/identifiers",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Identifier>> searchIdentifiers(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Identifiers for query {}", query);
        Page<Identifier> page = identifierSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/identifiers");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
