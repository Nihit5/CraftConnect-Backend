package com.nihit.craft_connect.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nihit.craft_connect.config.CustomMessageSource;
import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum ResponseStatus {
    FAILURE(0), SUCCESS(1);

    @Getter
    private final int index;
    private final CustomMessageSource customMessageSource = new CustomMessageSource();

    ResponseStatus(int index) {
        this.index = index;
    }

    public String getName() {
        if (this.equals(ResponseStatus.SUCCESS)) {
            return "success";
        } else return "failure";
    }

}
