package com.guthub.marinkay;

import com.guthub.marinkay.amqp.AmqpInitializator;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        LOGGER.info("START");
        AmqpInitializator amqpInitializator = new AmqpInitializator();


        amqpInitializator.stopAmqp();
    }
}