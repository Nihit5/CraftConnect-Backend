package com.nihit.craft_connect.dto.khalti;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KhaltiLookupRequest {
    private String pidx;

    public KhaltiLookupRequest(String pidx) {
        this.pidx = pidx;
    }
}
