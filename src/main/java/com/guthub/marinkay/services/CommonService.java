package com.guthub.marinkay.services;

import com.guthub.marinkay.amqp.AmqpProducer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.guthub.marinkay.dtos.Constants.QUEUE_URL_NAME;

public class CommonService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonService.class);

    public static void runHtmlParsing(Channel channel) {
        try {
            Map<String, Document> docs = new ConcurrentHashMap<>();
            HtmlServiceParserService htmlServiceParserService = new HtmlServiceParserService(docs);

            channel.basicConsume(QUEUE_URL_NAME, false, "consumerUrlQueue", new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body)
                        throws IOException {
                    long deliveryTag = envelope.getDeliveryTag();
                    String message = new String(body, StandardCharsets.UTF_8);
                    htmlServiceParserService.parseHtmlPage(message);
                    channel.basicAck(deliveryTag, false);
                }
            });
            Integer retryCount = 3;
            Integer responseWatingsCount = 0;
            while (responseWatingsCount < retryCount) {
                AMQP.Queue.DeclareOk response = channel.queueDeclarePassive(QUEUE_URL_NAME);
                if (response.getMessageCount() != 0) {
                    responseWatingsCount = 0;
                    Thread.sleep(100);
                } else {
                    Thread.sleep(1000);
                    responseWatingsCount++;
                    LOGGER.info("Waiting messages in " + QUEUE_URL_NAME);
                }
            }

            channel.basicCancel("consumerUrlQueue");

            AmqpProducer amqpProducer = new AmqpProducer(channel);
            amqpProducer.parseNews(docs);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private void produceMessagesToDataQueue(Map<String, Document> docs, Channel channel){

    }
}
