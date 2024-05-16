package com.guthub.marinkay.client;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;

import java.io.IOException;

import static com.guthub.marinkay.dtos.Constants.NEWS_HEADER_LINE_INDEX;


@RequiredArgsConstructor
@Data
public class ElasticDbClient {
    private final String serverUrl;
    private final String apiKey;

    public ElasticsearchClient createClient(){
        RestClient restClient = RestClient
                .builder(HttpHost.create(serverUrl))
                .setDefaultHeaders(new Header[]{
                        new BasicHeader("Authorization", "ApiKey " + apiKey)
                })
                .build();
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        ElasticsearchTransport elasticsearchTransport = new RestClientTransport(
                restClient, new JacksonJsonpMapper(objectMapper));
        ElasticsearchClient elasticsearchClient = new ElasticsearchClient(elasticsearchTransport);
        return elasticsearchClient;
    }
    public void createIndexesInElasticSearchDb(ElasticsearchClient elasticsearchClient){
        try {
            BooleanResponse indexRes = elasticsearchClient.indices().exists(ex -> ex.index(NEWS_HEADER_LINE_INDEX));
            if (!indexRes.value()) {
                elasticsearchClient.indices().create(c -> c
                        .index(NEWS_HEADER_LINE_INDEX)
                        .mappings(m -> m
                                .properties("id", p -> p.keyword(d -> d))
                                .properties("body", p -> p.text(d -> d.fielddata(true)))
                                .properties("header", p -> p.text(d -> d.fielddata(true)))
                                .properties("author", p -> p.text(d -> d.fielddata(true)))
                                .properties("URL", p -> p.keyword(d -> d))
                                .properties("date", p -> p.date(d -> d.format("strict_date_optional_time")))
                        ));
            }
        } catch (IOException e){
            e.printStackTrace();
            throw new RuntimeException("Something went wrong when create indexes");
        }

    }
}
