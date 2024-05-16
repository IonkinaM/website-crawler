package com.guthub.marinkay.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.jsoup.internal.StringUtil;

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
    private String date;
    @JsonProperty("URL")
    private String url;

    public void SetId() throws NoSuchAlgorithmException {
        if (StringUtil.isBlank(url)) throw new RuntimeException("Url is blank");
        this.id = Arrays.toString(MessageDigest.getInstance("MD5").digest(url.getBytes(StandardCharsets.UTF_8)));
    }
}
