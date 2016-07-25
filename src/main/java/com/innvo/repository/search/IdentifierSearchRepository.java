package com.innvo.repository.search;

import com.innvo.domain.Identifier;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Identifier entity.
 */
public interface IdentifierSearchRepository extends ElasticsearchRepository<Identifier, Long> {
}
