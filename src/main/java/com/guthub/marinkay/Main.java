package com.guthub.marinkay;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.guthub.marinkay.amqp.AmqpInitializator;
import com.guthub.marinkay.services.CommonService;
import com.guthub.marinkay.services.LinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.guthub.marinkay.client.ElasticDbClient.createClient;
import static com.guthub.marinkay.client.ElasticDbClient.createIndexesInElasticSearchDb;
import static com.guthub.marinkay.dtos.Constants.*;
import static com.guthub.marinkay.services.CommonService.runHtmlParsing;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        LOGGER.info("START");
        AmqpInitializator amqpInitializator = new AmqpInitializator();
        ElasticsearchClient elasticsearchClient = createClient(ELASTIC_URL,ELASTIC_API_KEY);
        createIndexesInElasticSearchDb(elasticsearchClient);
        LinkService linkService = new LinkService(URL_DEBPTH,WEBSITE_PATH,amqpInitializator.getChannel(),elasticsearchClient);
        linkService.processLink();
        runHtmlParsing(amqpInitializator.getChannel());
        amqpInitializator.stopAmqp();
    }
}