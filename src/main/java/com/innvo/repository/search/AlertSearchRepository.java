package com.innvo.repository.search;

import com.innvo.domain.Alert;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Alert entity.
 */
public interface AlertSearchRepository extends ElasticsearchRepository<Alert, Long> {
}
