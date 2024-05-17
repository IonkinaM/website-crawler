package com.guthub.marinkay.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.guthub.marinkay.dtos.HeaderLineDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static com.guthub.marinkay.client.ElasticDbClient.checkNewsHeaderLineExist;
import static com.guthub.marinkay.dtos.Constants.NEWS_HEADER_LINE_INDEX;

public class ElasticSearchService {
    private final ObjectMapper mapper = new ObjectMapper();
    private final ElasticsearchClient elcClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchService.class);

    public ElasticSearchService(ElasticsearchClient elcClient) throws IOException {
        this.elcClient = elcClient;
        mapper.registerModule(new JodaModule());
    }

    public void consume(String msg) throws IOException {
        try {
            LOGGER.debug(Thread.currentThread().getName() + "started");
            HeaderLineDto nh = new HeaderLineDto();
            JsonNode newsHeadlineJsonNode = mapper.readTree(msg);
            nh.setDate(newsHeadlineJsonNode.get("date").asText());
            nh.setUrl(newsHeadlineJsonNode.get("URL").asText());
            nh.setAuthor(newsHeadlineJsonNode.get("author").asText());
            nh.setBody(newsHeadlineJsonNode.get("body").asText());
            nh.setHeader(newsHeadlineJsonNode.get("header").asText());
            nh.SetId();
            if (!checkNewsHeaderLineExist(elcClient, nh.getId())) {
                IndexRequest<HeaderLineDto> indexReq = IndexRequest.of((id -> id
                        .index(NEWS_HEADER_LINE_INDEX)
                        .refresh(Refresh.WaitFor)
                        .document(nh)));
                IndexResponse indexResponse = elcClient.index(indexReq);
                if (indexResponse.result() != null) {
                    LOGGER.info("Document indexed successfully!");
                } else {
                    LOGGER.error("Error occurred during indexing!");
                }
            }
        } catch (IOException e) {
            LOGGER.error(String.valueOf(e));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        LOGGER.debug(Thread.currentThread().getName() + "stopped");
    }
}
