package com.guthub.marinkay.services;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class HtmlServiceParserService {
    private volatile Map<String, Document> docs;
    private CloseableHttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlServiceParserService.class);
    public HtmlServiceParserService(Map<String, Document> docs){
        this.docs=docs;
        client = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
                .setDefaultCookieStore(new BasicCookieStore()).build();
    }
    public void parseHtmlPage(String url) {
        try {
            LOGGER.info("Send request");
            boolean stopRetryFlag = false;
            int retryCount = 3;
            for (int iTry = 0; iTry < retryCount && !stopRetryFlag; iTry++) {
                RequestConfig requestConfig = RequestConfig.custom()
                        .setSocketTimeout(15000)
                        .setConnectTimeout(15000)
                        .setConnectionRequestTimeout(15000)
                        .setExpectContinueEnabled(true)
                        .build();
                HttpGet request = new HttpGet(url);
                request.setConfig(requestConfig);
                CloseableHttpResponse response = client.execute(request);
                Integer code = response.getStatusLine().getStatusCode();
                if(code == 200){
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        try {
                            Document doc = Jsoup.parse(entity.getContent(), "UTF-8", url);
                            docs.put(url, doc);
                            try {
                                response.close();
                            } catch (IOException e) {
                                LOGGER.error(String.valueOf(e));
                            }
                            break;
                        } catch (IOException e) {
                            LOGGER.error(String.valueOf(e));
                        }
                    }
                    stopRetryFlag = true;
                } else {
                    LOGGER.warn("Got status code not equals 200 from" + url + " code " + code);
                    response.close();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        break;
                    }
                    continue;
                }
                if (response != null) {
                    response.close();
                }
            }
        } catch (IOException e) {
            LOGGER.error(String.valueOf(e));
        }
    }


}
