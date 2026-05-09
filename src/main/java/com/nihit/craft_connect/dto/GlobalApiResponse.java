package com.nihit.craft_connect.dto;


import com.nihit.craft_connect.enums.ResponseStatus;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GlobalApiResponse implements Serializable {
    private String message;
    private Object data;
    private ResponseStatus status;
}
