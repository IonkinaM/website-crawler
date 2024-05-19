package com.guthub.marinkay.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.jsoup.internal.StringUtil;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HeaderLineDto {
    private String id;
    @Setter
    private String header;
    @Setter
    private String body;
    @Setter
    private String author;
    @Setter
    private String date;
    @JsonProperty("URL")
    @Setter
    private String url;

    public void SetId() throws NoSuchAlgorithmException {
        if (StringUtil.isBlank(url)) throw new RuntimeException("Url is blank");
        BigInteger bigInt = new BigInteger(1,MessageDigest.getInstance("MD5").digest(url.getBytes(StandardCharsets.UTF_8)));
        String hashtext = bigInt.toString(16);
        while(hashtext.length() < 32 ){
            hashtext = "0"+hashtext;
        }
        this.id = hashtext;
    }
}
