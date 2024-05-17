package com.guthub.marinkay.amqp;

import com.guthub.marinkay.services.LinkService;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static com.guthub.marinkay.dtos.Constants.*;

public class AmqpInitializator {
    private Connection connection;
    @Getter
    private Channel channel;
    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpInitializator.class);
    public AmqpInitializator(){
        initAmqp();
    }
    public void initAmqp(){
        LOGGER.info("Init AMQP");
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(RMQ_HOST_NAME);
            factory.setPort(RMQ_PORT);
            factory.setUsername(RMQ_USERNAME);
            factory.setPassword(RMQ_PASSWORD);
            connection = factory.newConnection();
            channel = connection.createChannel();

            channel.queueDeclare(QUEUE_DATA_NAME, true, false, false, null);
            channel.queueDeclare(QUEUE_URL_NAME, true, false, false, null);
            channel.basicQos(1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("AMQP Initiated");

    }
    public void stopAmqp(){
        LOGGER.info("STOP AMQP");
        try {
            channel.close();
            connection.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("AMQP STOPPED");
    }

}
