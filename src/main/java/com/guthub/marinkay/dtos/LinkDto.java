package com.guthub.marinkay.dtos;

import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Data
public class LinkDto {
    private String id;
    private Integer level;
    private String url;

    public LinkDto(String url, Integer level) throws NoSuchAlgorithmException {
        this.id = Arrays.toString(MessageDigest.getInstance("MD5").digest(url.getBytes(StandardCharsets.UTF_8)));
        this.url = url;
        this.level = level;
    }
}
