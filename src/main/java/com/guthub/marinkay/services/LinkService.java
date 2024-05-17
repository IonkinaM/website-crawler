package com.guthub.marinkay.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.guthub.marinkay.dtos.LinkDto;
import com.rabbitmq.client.Channel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.Deque;

import static com.guthub.marinkay.client.ElasticDbClient.checkNewsHeaderLineExist;
import static com.guthub.marinkay.dtos.Constants.QUEUE_URL_NAME;

public class LinkService {
    private String baseUrl;
    private Channel rmqChan;
    private ElasticsearchClient elcClient;
    private Integer depthValue;
    public Deque<LinkDto> urlQueue = new ArrayDeque<LinkDto>();
    public Deque<LinkDto> urlQueueResult = new ArrayDeque<LinkDto>();
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkService.class);

    public LinkService(int depth, String inputBaseUrl, Channel rmqChannel,ElasticsearchClient elasticsearchClient) {
        baseUrl = inputBaseUrl;
        urlQueue.add(new LinkDto(baseUrl, 0));
        depthValue = depth;
        rmqChan = rmqChannel;
        elcClient = elasticsearchClient;
    }
    public void processLink() {
        for (int i = 0; i < depthValue; ++i) {
            parse();
            LOGGER.info(Thread.currentThread().getName() + "START");
            urlQueue.addAll(urlQueueResult);
        }
        LOGGER.info(Thread.currentThread() + "END, processed " + urlQueue.size() + " links");
    }
    private void parse(){
        LinkDto linkDto;
        while ((linkDto = urlQueue.pollFirst()) != null) {
            parseLink(linkDto);
        }
    }
    private void parseLink(LinkDto linkDto){
        try {
            Integer level = linkDto.getLevel() + 1;
            Document doc = Jsoup.connect(linkDto.getUrl()).get();
            Elements links = doc.select("a[href]");
            String newUrl;
            for (Element link : links) {
                newUrl = link.attr("abs:href");
                if (
                        !newUrl.startsWith(baseUrl + "/politics/2024/") &&
                                !newUrl.startsWith(baseUrl + "/incident/2024/") &&
                                !newUrl.startsWith(baseUrl + "/culture/2024/") &&
                                !newUrl.startsWith(baseUrl + "/social/2024/") &&
                                !newUrl.startsWith(baseUrl + "/economics/2024/") &&
                                !newUrl.startsWith(baseUrl + "/science/2024/") &&
                                !newUrl.startsWith(baseUrl + "/sport/2024/")
                ) {
                    continue;
                }
                if (newUrl.endsWith("#")) {
                    newUrl = newUrl.substring(0, newUrl.length() - 1);
                }
                LinkDto linkDtoNew = new LinkDto(newUrl, level);
                if (!checkNewsHeaderLineExist(elcClient, linkDtoNew.getId())) {
                    rmqChan.basicPublish("", QUEUE_URL_NAME, null, newUrl.getBytes(StandardCharsets.UTF_8));
                    urlQueueResult.add(linkDtoNew);
                }

                if (level <= this.depthValue) {
                    urlQueue.add(linkDtoNew);
                }
            }
        } catch (IOException e){
            e.printStackTrace();
            throw new RuntimeException("Something went wrong when parse link");
        }

    }
}
