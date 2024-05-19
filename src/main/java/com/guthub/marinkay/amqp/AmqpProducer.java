package com.guthub.marinkay.amqp;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guthub.marinkay.dtos.HeaderLineDto;
import com.rabbitmq.client.Channel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import static com.guthub.marinkay.dtos.Constants.QUEUE_DATA_NAME;

public class AmqpProducer {
    private Channel amqpChannel;
    private  ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpProducer.class);
    public AmqpProducer(Channel channel) {
        amqpChannel = channel;
    }
    public void parseNews(Map<String, Document> docs) throws InterruptedException, IOException {
        if (docs.isEmpty()) {
            LOGGER.warn("Docs is empty");
        } else {
            for (Map.Entry<String, Document> entry : docs.entrySet()) {
                mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
                produceToQueue(entry.getKey(), entry.getValue());
            }
        }
    }
    private void produceToQueue(String url, Document doc) throws IOException {
        try {
            HeaderLineDto newsHeadline = new HeaderLineDto();
            newsHeadline.setHeader(doc.select("div [class=WidgetArticle__root--9bI7h]").get(0).text());
            newsHeadline.setAuthor(doc.select("li [class=WidgetArticle__authors--RQEI2__name]").get(0).text());
            newsHeadline.setBody(doc.select("div [class=article__content js-mediator-article]").get(0).text());
            String date = doc.attr("article:published_time");
            newsHeadline.setDate(date);
            newsHeadline.setUrl(url);
            newsHeadline.SetId();
            amqpChannel.basicPublish("", QUEUE_DATA_NAME, null, mapper.writeValueAsBytes(newsHeadline));
        } catch (Exception e) {
            LOGGER.error(String.valueOf(e));
        }
    }
}
