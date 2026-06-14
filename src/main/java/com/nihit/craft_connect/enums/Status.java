package com.nihit.craft_connect.enums;

import com.nihit.craft_connect.dto.KeyValuePojo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Status {

    PENDING("PENDING", "Pending"),
    APPROVED("APPROVED", "Approved"),
    REJECTED("REJECTED", "Rejected"),
    SUSPENDED("SUSPENDED", "Suspended");

    private final String key;
    private final String value;

    Status(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public static List<KeyValuePojo> getStatusList() {
        return Arrays.stream(Status.values())
                .map(x -> new KeyValuePojo(x.value, x.name()))
                .collect(Collectors.toList());
    }
}